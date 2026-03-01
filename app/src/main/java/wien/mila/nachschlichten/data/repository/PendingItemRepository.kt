package wien.mila.nachschlichten.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wien.mila.nachschlichten.data.local.dao.PendingItemDao
import wien.mila.nachschlichten.data.local.dao.PendingItemWithArticle
import wien.mila.nachschlichten.data.local.dao.ZonePendingCount
import wien.mila.nachschlichten.data.local.entity.PendingItemEntity
import wien.mila.nachschlichten.domain.model.PendingItem
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PendingItemRepository @Inject constructor(
    private val pendingItemDao: PendingItemDao
) {
    fun getByShelf(shelfId: String): Flow<List<PendingItem>> =
        pendingItemDao.getByShelf(shelfId).map { list -> list.map { it.toModel() } }

    fun getAllPending(): Flow<List<PendingItem>> =
        pendingItemDao.getAllPending().map { list -> list.map { it.toModel() } }

    fun getByZone(zoneId: String): Flow<List<PendingItem>> =
        pendingItemDao.getByZone(zoneId).map { list -> list.map { it.toModel() } }

    fun getPendingCountByZone(): Flow<List<ZonePendingCount>> =
        pendingItemDao.getPendingCountByZone()

    fun getPendingCount(): Flow<Int> = pendingItemDao.getPendingCount()
    fun getDoneCount(): Flow<Int> = pendingItemDao.getDoneCount()

    suspend fun insert(articleId: Long, shelfId: String, quantity: Int?): Long {
        return pendingItemDao.insert(
            PendingItemEntity(
                articleId = articleId,
                shelfId = shelfId,
                quantity = quantity,
                createdAt = System.currentTimeMillis()
            )
        )
    }

    suspend fun markDone(id: Long) {
        pendingItemDao.markDone(id)
    }

    suspend fun deleteAllPendingForShelf(shelfId: String) {
        pendingItemDao.deleteAllPendingForShelf(shelfId)
    }

    suspend fun deleteAllPending() {
        pendingItemDao.deleteAllPending()
    }

    suspend fun getById(id: Long): PendingItem? = pendingItemDao.getById(id)?.toModel()

    private fun PendingItemWithArticle.toModel() = PendingItem(
        id = id,
        articleId = articleId,
        articleName = articleName,
        articleEan = articleEan,
        shelfId = shelfId,
        quantity = quantity,
        createdAt = createdAt,
        isDone = isDone
    )
}
