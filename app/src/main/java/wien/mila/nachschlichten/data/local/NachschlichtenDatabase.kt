package wien.mila.nachschlichten.data.local

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
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
    version = 4,
    autoMigrations = [],
    exportSchema = true
)
abstract class NachschlichtenDatabase : RoomDatabase() {
    abstract fun shelfDao(): ShelfDao
    abstract fun storageZoneDao(): StorageZoneDao
    abstract fun articleDao(): ArticleDao
    abstract fun pendingItemDao(): PendingItemDao

    companion object {
        val MIGRATION_3_4 = object : Migration(3, 4) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQLite can't ALTER COLUMN TYPE; recreate the table.
                db.execSQL("""
             CREATE TABLE pending_items_new (
                 id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                 articleId INTEGER NOT NULL,
                 shelfId TEXT NOT NULL,
                 quantity INTEGER,
                 createdAt INTEGER NOT NULL,
                 isDone INTEGER NOT NULL DEFAULT 0,
                 FOREIGN KEY(articleId) REFERENCES articles(id),
                 FOREIGN KEY(shelfId) REFERENCES shelves(id)
             )
         """)
                db.execSQL("""
             INSERT INTO pending_items_new
             SELECT id, articleId, shelfId,
                    CAST(quantity AS INTEGER), createdAt, isDone
             FROM pending_items
         """)
                db.execSQL("DROP TABLE pending_items")
                db.execSQL("ALTER TABLE pending_items_new RENAME TO pending_items")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pending_items_articleId ON pending_items(articleId)")
                db.execSQL("CREATE INDEX IF NOT EXISTS index_pending_items_shelfId ON pending_items(shelfId)")
            }
        }

    }
}

