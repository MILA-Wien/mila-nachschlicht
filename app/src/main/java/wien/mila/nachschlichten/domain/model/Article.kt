package wien.mila.nachschlichten.domain.model

data class Article(
    val id: Long,
    val ean: String,
    val name: String,
    val unit: String,
    val totalStock: Int,
    val price: Double,
    val lastSyncedAt: Long,
    val imagePath: String?
)
