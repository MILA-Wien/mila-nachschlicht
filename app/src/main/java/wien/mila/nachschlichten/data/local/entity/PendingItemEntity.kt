package wien.mila.nachschlichten.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "pending_items",
    foreignKeys = [
        ForeignKey(
            entity = ArticleEntity::class,
            parentColumns = ["id"],
            childColumns = ["articleId"]
        ),
        ForeignKey(
            entity = ShelfEntity::class,
            parentColumns = ["id"],
            childColumns = ["shelfId"]
        )
    ],
    indices = [Index("articleId"), Index("shelfId")]
)
data class PendingItemEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val articleId: Long,
    val shelfId: String,
    val quantity: Int?,
    val createdAt: Long,
    val isDone: Boolean = false
)
