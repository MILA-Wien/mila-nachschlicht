package wien.mila.nachschlichten.data.transfer

data class TransferFile(
    val version: Int = 1,
    val exportedAt: Long,
    val apiSettings: TransferApiSettings?,
    val zones: List<TransferZone>?,
    val shelves: List<TransferShelf>?,
    val articleImages: List<TransferArticleImage>?,
    val pendingItems: List<TransferPendingItem>?
)

data class TransferOptions(
    val includeApiSettings: Boolean = true,
    val includeZonesAndShelves: Boolean = true,
    val includeArticleImages: Boolean = true,
    val includePendingItems: Boolean = false,
    val deleteAbsentZonesAndShelves: Boolean = false
)

data class TransferApiSettings(val apiUrl: String, val username: String)
data class TransferZone(val id: String, val description: String, val color: String)
data class TransferShelf(val id: String, val description: String, val storageZoneId: String?)

// Exactly one of imagePath or imageData is non-null
data class TransferArticleImage(val articleId: Long, val imagePath: String?, val imageData: String?)

data class TransferPendingItem(
    val articleId: Long,
    val shelfId: String,
    val quantity: Int?,
    val createdAt: Long,
    val isDone: Boolean
)

data class ImportResult(val skippedPendingItems: Int)
