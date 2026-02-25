package wien.mila.nachschlichten.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import wien.mila.nachschlichten.data.local.dao.ArticleDao
import wien.mila.nachschlichten.data.local.dao.PendingItemDao
import wien.mila.nachschlichten.data.local.dao.ShelfDao
import wien.mila.nachschlichten.data.local.dao.StorageZoneDao
import wien.mila.nachschlichten.data.local.entity.ArticleEntity
import wien.mila.nachschlichten.data.local.entity.PendingItemEntity
import wien.mila.nachschlichten.data.local.entity.ShelfEntity
import wien.mila.nachschlichten.data.local.entity.StorageZoneEntity

@Database(
    entities = [
        ShelfEntity::class,
        StorageZoneEntity::class,
        ArticleEntity::class,
        PendingItemEntity::class
    ],
    version = 2,
    exportSchema = true
)
abstract class NachschlichtenDatabase : RoomDatabase() {
    abstract fun shelfDao(): ShelfDao
    abstract fun storageZoneDao(): StorageZoneDao
    abstract fun articleDao(): ArticleDao
    abstract fun pendingItemDao(): PendingItemDao
}
