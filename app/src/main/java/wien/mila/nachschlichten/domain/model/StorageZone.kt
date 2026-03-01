package wien.mila.nachschlichten.domain.model

data class StorageZone(
    val id: String,
    val description: String,
    val color: String
) {
    val reprString: String
        get() = "$id $description"
}
