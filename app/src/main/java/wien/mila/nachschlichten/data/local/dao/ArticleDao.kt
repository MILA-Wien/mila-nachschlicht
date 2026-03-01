package wien.mila.nachschlichten.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wien.mila.nachschlichten.data.local.entity.ArticleEntity

data class ArticleImageRow(val id: Long, val imagePath: String?)

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE ean = :ean LIMIT 1")
    suspend fun getByEan(ean: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: Long): ArticleEntity?

    @Query("SELECT * FROM articles ORDER BY name")
    fun getAll(): Flow<List<ArticleEntity>>

    @Query("SELECT COUNT(*) FROM articles")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM articles WHERE ean != ''")
    fun countWithEan(): Flow<Int>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Query("SELECT id, imagePath FROM articles WHERE imagePath IS NOT NULL")
    suspend fun getAllWithImagePath(): List<ArticleImageRow>

    @Query("UPDATE articles SET imagePath = :imagePath WHERE id = :id")
    suspend fun updateImagePath(id: Long, imagePath: String)
}
