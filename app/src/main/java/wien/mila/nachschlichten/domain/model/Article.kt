package wien.mila.nachschlichten.domain.model

data class Article(
    val id: Long,
    val eans: List<String>,
    val name: String,
    val unit: String,
    val totalStock: Double,
    val price: Double,
    val lastSyncedAt: Long,
    val imagePath: String?
)
