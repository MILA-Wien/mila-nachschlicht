package wien.mila.nachschlichten.data.repository

import wien.mila.nachschlichten.data.local.dao.ArticleDao
import wien.mila.nachschlichten.data.local.entity.ArticleEntity
import wien.mila.nachschlichten.data.remote.NachschlichtenApi
import wien.mila.nachschlichten.domain.model.Article
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ArticleRepository @Inject constructor(
    private val articleDao: ArticleDao,
    private val api: NachschlichtenApi
) {
    suspend fun getByEan(ean: String): Article? {
        return articleDao.getByEan(ean)?.toModel()
    }

    suspend fun getById(id: Long): Article? {
        return articleDao.getById(id)?.toModel()
    }

    suspend fun syncFromApi(): Result<Int> {
        return try {
            val dtos = api.getArticles()
            val now = System.currentTimeMillis()
            val entities = dtos.map { dto ->
                val existing = articleDao.getById(dto.id)
                ArticleEntity(
                    id = dto.id,
                    ean = dto.ean,
                    name = dto.name,
                    unit = dto.unit,
                    totalStock = dto.totalStock,
                    price = dto.price,
                    lastSyncedAt = now,
                    imagePath = existing?.imagePath
                )
            }
            articleDao.upsertAll(entities)
            Result.success(entities.size)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateImagePath(articleId: Long, imagePath: String) {
        val article = articleDao.getById(articleId) ?: return
        articleDao.upsertAll(listOf(article.copy(imagePath = imagePath)))
    }

    private fun ArticleEntity.toModel() = Article(
        id = id,
        ean = ean,
        name = name,
        unit = unit,
        totalStock = totalStock,
        price = price,
        lastSyncedAt = lastSyncedAt,
        imagePath = imagePath
    )
}
