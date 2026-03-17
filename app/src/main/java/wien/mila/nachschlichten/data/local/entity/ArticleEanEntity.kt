package wien.mila.nachschlichten.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "article_eans",
    foreignKeys = [ForeignKey(
        entity = ArticleEntity::class,
        parentColumns = ["id"], childColumns = ["articleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class ArticleEanEntity(
    @PrimaryKey val ean: String,
    @ColumnInfo(index = true) val articleId: Long
)
