package wien.mila.nachschlichten.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "articles")
data class ArticleEntity(
    @PrimaryKey val id: Long,
    val name: String,
    val unit: String,
    val totalStock: Double,
    val price: Double,
    val lastSyncedAt: Long,
    val imagePath: String?
)
