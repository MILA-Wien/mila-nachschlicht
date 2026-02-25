package wien.mila.nachschlichten.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "storage_zones")
data class StorageZoneEntity(
    @PrimaryKey val id: String,
    val name: String,
    val color: String
)
