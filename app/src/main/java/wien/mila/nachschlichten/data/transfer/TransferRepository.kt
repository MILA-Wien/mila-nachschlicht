package wien.mila.nachschlichten.data.transfer

import android.content.Context
import android.util.Base64
import androidx.core.content.FileProvider
import dagger.hilt.android.qualifiers.ApplicationContext
import wien.mila.nachschlichten.data.datastore.UserPreferences
import wien.mila.nachschlichten.data.local.dao.ArticleDao
import wien.mila.nachschlichten.data.local.dao.PendingItemDao
import wien.mila.nachschlichten.data.local.dao.ShelfDao
import wien.mila.nachschlichten.data.local.dao.StorageZoneDao
import wien.mila.nachschlichten.data.local.entity.PendingItemEntity
import wien.mila.nachschlichten.data.local.entity.ShelfEntity
import wien.mila.nachschlichten.data.local.entity.StorageZoneEntity
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransferRepository @Inject constructor(
    @param:ApplicationContext val context: Context,
    val userPreferences: UserPreferences,
    val storageZoneDao: StorageZoneDao,
    val shelfDao: ShelfDao,
    val articleDao: ArticleDao,
    val pendingItemDao: PendingItemDao
) {
    suspend fun buildExport(options: TransferOptions): TransferFile {
        val apiSettings = if (options.includeApiSettings) {
            TransferApiSettings(
                apiUrl = userPreferences.getApiUrlOnce(),
                username = userPreferences.getUsernameOnce()
            )
        } else null

        val zones = if (options.includeZonesAndShelves) {
            storageZoneDao.getAllOnce().map { TransferZone(it.id, it.description, it.color) }
        } else null

        val shelves = if (options.includeZonesAndShelves) {
            shelfDao.getAllOnce().map { TransferShelf(it.id, it.description, it.storageZoneId) }
        } else null

        val articleImages = if (options.includeArticleImages) {
            articleDao.getAllWithImagePath().mapNotNull { row ->
                val path = row.imagePath ?: return@mapNotNull null
                when {
                    path.startsWith("https://") -> TransferArticleImage(row.id, path, null)
                    path.startsWith("content://") -> {
                        val filename = path.substringAfterLast("/")
                        val file = File(context.filesDir, "article_images/$filename")
                        if (file.exists()) {
                            val bytes = file.readBytes()
                            val encoded = Base64.encodeToString(bytes, Base64.NO_WRAP)
                            TransferArticleImage(row.id, null, encoded)
                        } else null
                    }
                    else -> null
                }
            }
        } else null

        val pendingItems = if (options.includePendingItems) {
            pendingItemDao.getAllEntities().map {
                TransferPendingItem(it.articleId, it.shelfId, it.quantity, it.createdAt, it.isDone)
            }
        } else null

        return TransferFile(
            exportedAt = System.currentTimeMillis(),
            apiSettings = apiSettings,
            zones = zones,
            shelves = shelves,
            articleImages = articleImages,
            pendingItems = pendingItems
        )
    }

    suspend fun applyImport(file: TransferFile, options: TransferOptions): ImportResult {
        if (options.includeApiSettings && file.apiSettings != null) {
            userPreferences.setApiUrl(file.apiSettings.apiUrl)
            userPreferences.setUsername(file.apiSettings.username)
        }

        if (options.includeZonesAndShelves) {
            file.zones?.forEach { zone ->
                storageZoneDao.upsert(StorageZoneEntity(zone.id, zone.description, zone.color))
            }
            file.shelves?.forEach { shelf ->
                shelfDao.upsert(ShelfEntity(shelf.id, shelf.description, shelf.storageZoneId))
            }

            if (options.deleteAbsentZonesAndShelves) {
                val importShelfIds = file.shelves?.map { it.id }?.toSet() ?: emptySet()
                shelfDao.getAllOnce()
                    .filter { it.id !in importShelfIds }
                    .forEach { shelf ->
                        pendingItemDao.deleteAllItemsForShelf(shelf.id)
                        shelfDao.delete(shelf.id)
                    }

                val importZoneIds = file.zones?.map { it.id }?.toSet() ?: emptySet()
                storageZoneDao.getAllOnce()
                    .filter { it.id !in importZoneIds }
                    .forEach { zone -> storageZoneDao.delete(zone.id) }
            }
        }

        if (options.includeArticleImages && file.articleImages != null) {
            val imageDir = File(context.filesDir, "article_images")
            imageDir.mkdirs()
            for (entry in file.articleImages) {
                val articleExists = articleDao.getById(entry.articleId) != null
                if (!articleExists) continue
                when {
                    entry.imagePath != null -> {
                        articleDao.updateImagePath(entry.articleId, entry.imagePath)
                    }
                    entry.imageData != null -> {
                        val bytes = Base64.decode(entry.imageData, Base64.NO_WRAP)
                        val newFile = File(imageDir, "${UUID.randomUUID()}.jpg")
                        newFile.writeBytes(bytes)
                        val uri = FileProvider.getUriForFile(
                            context,
                            "${context.packageName}.fileprovider",
                            newFile
                        )
                        articleDao.updateImagePath(entry.articleId, uri.toString())
                    }
                }
            }
        }

        var skippedPendingItems = 0
        if (options.includePendingItems && file.pendingItems != null) {
            for (item in file.pendingItems) {
                val entity = PendingItemEntity(
                    id = 0,
                    articleId = item.articleId,
                    shelfId = item.shelfId,
                    quantity = item.quantity,
                    createdAt = item.createdAt,
                    isDone = item.isDone
                )
                val rowId = pendingItemDao.insertIgnoreConflict(entity)
                if (rowId == -1L) skippedPendingItems++
            }
        }

        return ImportResult(skippedPendingItems = skippedPendingItems)
    }
}
