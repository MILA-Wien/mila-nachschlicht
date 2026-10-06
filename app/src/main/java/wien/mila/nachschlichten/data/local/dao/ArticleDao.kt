package wien.mila.nachschlichten.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wien.mila.nachschlichten.data.local.entity.ArticleEanEntity
import wien.mila.nachschlichten.data.local.entity.ArticleEntity

data class ArticleImageRow(val id: Long, val imagePath: String?)

@Dao
interface ArticleDao {
    @Query("SELECT a.* FROM articles a INNER JOIN article_eans e ON e.articleId = a.id WHERE e.ean = :ean LIMIT 1")
    suspend fun getByEan(ean: String): ArticleEntity?

    @Query("SELECT * FROM articles WHERE id = :id")
    suspend fun getById(id: Long): ArticleEntity?

    @Query("SELECT * FROM articles ORDER BY name")
    fun getAll(): Flow<List<ArticleEntity>>

    @Query("SELECT COUNT(*) FROM articles")
    fun countAll(): Flow<Int>

    @Query("SELECT COUNT(*) FROM article_eans")
    fun countWithEan(): Flow<Int>

    @Upsert
    suspend fun upsertAll(articles: List<ArticleEntity>)

    @Upsert
    suspend fun upsertAllEans(eans: List<ArticleEanEntity>)

    @Query("DELETE FROM article_eans")
    suspend fun deleteAllEans()

    @Query("SELECT ean FROM article_eans WHERE articleId = :articleId")
    suspend fun getEansForArticle(articleId: Long): List<String>

    @Query("SELECT id, imagePath FROM articles WHERE imagePath IS NOT NULL")
    suspend fun getAllWithImagePath(): List<ArticleImageRow>

    @Query("UPDATE articles SET imagePath = :imagePath WHERE id = :id")
    suspend fun updateImagePath(id: Long, imagePath: String)
}
