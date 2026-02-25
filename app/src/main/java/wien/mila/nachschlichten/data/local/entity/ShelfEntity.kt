package wien.mila.nachschlichten.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "shelves")
data class ShelfEntity(
    @PrimaryKey val id: String,
    val name: String,
    val storageZoneId: String
)
