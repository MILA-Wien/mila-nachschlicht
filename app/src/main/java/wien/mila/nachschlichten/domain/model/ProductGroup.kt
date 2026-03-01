package wien.mila.nachschlichten.domain.model

data class ProductGroup(
    val zone: StorageZone?,
    val pendingCount: Int,
    val shelves: List<Shelf>
)
