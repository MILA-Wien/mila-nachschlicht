package wien.mila.nachschlichten.domain.model

data class PendingItem(
    val id: Long,
    val articleId: Long,
    val articleName: String,
    val articleEan: String,
    val shelfId: String,
    val quantity: Int?,
    val createdAt: Long,
    val isDone: Boolean,
    val imagePath: String?
)
