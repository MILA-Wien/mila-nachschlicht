package wien.mila.nachschlichten.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wien.mila.nachschlichten.data.local.entity.ArticleEntity

@Dao
interface ArticleDao {
    @Query("SELECT * FROM articles WHERE ean = :ean LIMIT 1")
    suspend fun getByEan(ean: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: Long): ArticleEntity?

    @Query("SELECT * FROM articles ORDER BY name")
    fun getAll(): Flow<List<ArticleEntity>>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)
}
