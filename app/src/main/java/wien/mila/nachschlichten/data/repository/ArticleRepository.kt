package wien.mila.nachschlichten.data.repository

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import retrofit2.HttpException
import wien.mila.nachschlichten.data.local.dao.ArticleDao
import wien.mila.nachschlichten.data.local.entity.ArticleEanEntity
import wien.mila.nachschlichten.data.local.entity.ArticleEntity
import wien.mila.nachschlichten.data.remote.NachschlichtenApi
import wien.mila.nachschlichten.domain.model.Article
import java.io.File
import javax.inject.Inject
import javax.inject.Named
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val api: NachschlichtenApi,
    @Named("plain") private val plainHttpClient: OkHttpClient,
    @param:ApplicationContext private val context: Context
) {
    fun countAll() = articleDao.countAll()
    fun countWithEan() = articleDao.countWithEan()

    suspend fun getByEan(ean: String): Article? {
        // First, try the EAN exactly as provided.
        var entity = articleDao.getByEan(ean)
        // If it's 12 digits, try prepending a zero.
        if (entity == null && ean.length == 12) {
            entity = articleDao.getByEan("0$ean")
        }
        // If it's 13 digits starting with zero, try dropping the leading zero.
        if (entity == null && ean.length == 13 && ean.startsWith("0")) {
            articleDao.getByEan(ean.substring(1))
        }

        if (entity == null) return null;

        val eans = articleDao.getEansForArticle(entity.id)
        return entity.toModel(eans)
    }

    suspend fun getById(id: Long): Article? {
        val entity = articleDao.getById(id) ?: return null
        val eans = articleDao.getEansForArticle(entity.id)
        return entity.toModel(eans)
    }

    suspend fun syncFromApi(url: String): Result<Int> {
        return try {
            val dtos = api.getArticles(url)
            val now = System.currentTimeMillis()
            val entities = dtos.map { dto ->
                val existing = articleDao.getById(dto.id)
                ArticleEntity(
                    id = dto.id,
                    name = dto.shortName.ifEmpty { dto.name },
                    unit = dto.unit ?: "",
                    totalStock = dto.totalStock,
                    price = dto.priceNet * (1 + dto.taxPercentage / 100.0),
                    lastSyncedAt = now,
                    imagePath = existing?.imagePath
                )
            }
            articleDao.upsertAll(entities)
            articleDao.deleteAllEans()
            val eanEntities = dtos.flatMap { dto -> dto.eans.orEmpty().map { ArticleEanEntity(it, dto.id) } }
            articleDao.upsertAllEans(eanEntities)
            Result.success(entities.size)
        } catch (e: HttpException) {
            Result.failure(Exception("HTTP ${e.code()} ${e.message()} — $url", e))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun deleteLocalImageFile(imagePath: String?) {
        if (imagePath == null || !imagePath.startsWith("content://")) return
        val filename = imagePath.substringAfterLast("/")
        File(context.filesDir, "article_images/$filename").delete()
    }

    suspend fun updateImagePath(articleId: Long, imagePath: String) {
        val article = articleDao.getById(articleId) ?: return
        deleteLocalImageFile(article.imagePath)
        articleDao.upsertAll(listOf(article.copy(imagePath = imagePath)))
    }

    suspend fun cleanupOrphanedImageFiles() {
        val referencedFilenames = articleDao.getAllWithImagePath()
            .mapNotNull { row ->
                val path = row.imagePath ?: return@mapNotNull null
                if (path.startsWith("content://")) path.substringAfterLast("/") else null
            }
            .toSet()
        val imageDir = File(context.filesDir, "article_images")
        imageDir.listFiles()?.forEach { file ->
            if (file.name !in referencedFilenames) file.delete()
        }
    }

    suspend fun fetchAndSaveImageFromOpenFoodFacts(eans: List<String>, articleId: Long): String? {
        return withContext(Dispatchers.IO) {
            for (ean in eans) {
                try {
                    val url = "https://world.openfoodfacts.org/api/v3/product/$ean.json?fields=image_url"
                    val request = Request.Builder().url(url).build()
                    val body = plainHttpClient.newCall(request).execute()
                        .takeIf { it.isSuccessful }?.body?.string() ?: continue
                    val json = JSONObject(body)
                    if (!json.optString("status").startsWith("success")) continue
                    val imageUrl = json.optJSONObject("product")
                        ?.optString("image_url")?.takeIf { it.isNotBlank() } ?: continue
                    updateImagePath(articleId, imageUrl)
                    return@withContext imageUrl
                } catch (_: Exception) {
                    continue
                }
            }
            null
        }
    }

    private fun ArticleEntity.toModel(eans: List<String>) = Article(
        id = id,
        eans = eans,
        name = name,
        unit = unit,
        totalStock = totalStock,
        price = price,
        lastSyncedAt = lastSyncedAt,
        imagePath = imagePath
    )
}
