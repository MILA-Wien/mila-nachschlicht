package wien.mila.nachschlichten.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import wien.mila.nachschlichten.data.local.entity.PendingItemEntity

data class PendingItemWithArticle(
    val id: Long,
    val articleId: Long,
    val articleName: String,
    val articleEan: String,
    val shelfId: String,
    val quantity: Int?,
    val createdAt: Long,
    val isDone: Boolean
)

data class ZonePendingCount(
    val storageZoneId: String,
    val pendingCount: Int
)

@Dao
interface PendingItemDao {
    @Query("""
        SELECT p.id, p.articleId, a.name AS articleName, a.ean AS articleEan,
               p.shelfId, p.quantity, p.createdAt, p.isDone
        FROM pending_items p
        INNER JOIN articles a ON a.id = p.articleId
        WHERE p.shelfId = :shelfId AND p.isDone = 0
        ORDER BY p.createdAt DESC
    """)
    fun getByShelf(shelfId: String): Flow<List<PendingItemWithArticle>>

    @Query("""
        SELECT p.id, p.articleId, a.name AS articleName, a.ean AS articleEan,
               p.shelfId, p.quantity, p.createdAt, p.isDone
        FROM pending_items p
        INNER JOIN articles a ON a.id = p.articleId
        WHERE p.isDone = 0
        ORDER BY p.createdAt DESC
    """)
    fun getAllPending(): Flow<List<PendingItemWithArticle>>

    @Query("""
        SELECT p.id, p.articleId, a.name AS articleName, a.ean AS articleEan,
               p.shelfId, p.quantity, p.createdAt, p.isDone
        FROM pending_items p
        INNER JOIN articles a ON a.id = p.articleId
        INNER JOIN shelves s ON s.id = p.shelfId
        WHERE s.storageZoneId = :zoneId
        ORDER BY p.isDone ASC, p.createdAt DESC
    """)
    fun getByZone(zoneId: String): Flow<List<PendingItemWithArticle>>

    @Query("""
        SELECT s.storageZoneId, COUNT(p.id) AS pendingCount
        FROM shelves s
        LEFT JOIN pending_items p ON p.shelfId = s.id AND p.isDone = 0
        GROUP BY s.storageZoneId
    """)
    fun getPendingCountByZone(): Flow<List<ZonePendingCount>>

    @Query("SELECT COUNT(*) FROM pending_items WHERE isDone = 0")
    fun getPendingCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM pending_items WHERE isDone = 1")
    fun getDoneCount(): Flow<Int>

    @Insert
    suspend fun insert(item: PendingItemEntity): Long

    @Query("UPDATE pending_items SET isDone = 1 WHERE id = :id")
    suspend fun markDone(id: Long)

    @Query("DELETE FROM pending_items WHERE shelfId = :shelfId AND isDone = 0")
    suspend fun deleteAllPendingForShelf(shelfId: String)

    @Query("DELETE FROM pending_items")
    suspend fun deleteAllPending()

    @Query("""
        SELECT p.id, p.articleId, a.name AS articleName, a.ean AS articleEan,
               p.shelfId, p.quantity, p.createdAt, p.isDone
        FROM pending_items p
        INNER JOIN articles a ON a.id = p.articleId
        WHERE p.id = :id
    """)
    suspend fun getById(id: Long): PendingItemWithArticle?

    @Query("""
        SELECT p.id, p.articleId, a.name AS articleName, a.ean AS articleEan,
               p.shelfId, p.quantity, p.createdAt, p.isDone
        FROM pending_items p
        INNER JOIN articles a ON a.id = p.articleId
        WHERE p.articleId = :articleId AND p.shelfId = :shelfId AND p.isDone = 0
        LIMIT 1
    """)
    suspend fun getByArticleAndShelf(articleId: Long, shelfId: String): PendingItemWithArticle?

    @Query("UPDATE pending_items SET quantity = :quantity WHERE id = :id")
    suspend fun updateQuantity(id: Long, quantity: Int?)

    @Query("DELETE FROM pending_items WHERE id = :id")
    suspend fun deleteById(id: Long)
}
