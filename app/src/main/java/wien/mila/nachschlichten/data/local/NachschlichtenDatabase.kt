package wien.mila.nachschlichten.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import wien.mila.nachschlichten.data.local.dao.ArticleDao
import wien.mila.nachschlichten.data.local.dao.PendingItemDao
import wien.mila.nachschlichten.data.local.dao.ShelfDao
import wien.mila.nachschlichten.data.local.dao.StorageZoneDao
import wien.mila.nachschlichten.data.local.entity.ArticleEanEntity
import wien.mila.nachschlichten.data.local.entity.ArticleEntity
import wien.mila.nachschlichten.data.local.entity.PendingItemEntity
import wien.mila.nachschlichten.data.local.entity.ShelfEntity
import wien.mila.nachschlichten.data.local.entity.StorageZoneEntity

@Database(
    entities = [
        ShelfEntity::class,
        StorageZoneEntity::class,
        ArticleEntity::class,
        ArticleEanEntity::class,
        PendingItemEntity::class
    ],
    version = 6,
    autoMigrations = [],
    exportSchema = true
)
abstract class NachschlichtenDatabase : RoomDatabase() {
    abstract fun shelfDao(): ShelfDao
    abstract fun storageZoneDao(): StorageZoneDao
    abstract fun articleDao(): ArticleDao
    abstract fun pendingItemDao(): PendingItemDao

    companion object {
        val MIGRATION_5_6 = object : Migration(5, 6) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // Create article_eans join table
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `article_eans` (
                        `ean` TEXT NOT NULL,
                        `articleId` INTEGER NOT NULL,
                        PRIMARY KEY(`ean`),
                        FOREIGN KEY(`articleId`) REFERENCES `articles`(`id`)
                            ON UPDATE NO ACTION ON DELETE CASCADE
                    )
                """)
                db.execSQL("CREATE INDEX IF NOT EXISTS `index_article_eans_articleId` ON `article_eans` (`articleId`)")
                // Copy existing single EANs into the join table
                db.execSQL("INSERT OR IGNORE INTO `article_eans` (`ean`, `articleId`) SELECT `ean`, `id` FROM `articles` WHERE `ean` != ''")
                // Recreate articles without the ean column (ALTER TABLE DROP COLUMN not universally supported)
                db.execSQL("""
                    CREATE TABLE `articles_new` (
                        `id` INTEGER NOT NULL,
                        `name` TEXT NOT NULL,
                        `unit` TEXT NOT NULL,
                        `totalStock` REAL NOT NULL,
                        `price` REAL NOT NULL,
                        `lastSyncedAt` INTEGER NOT NULL,
                        `imagePath` TEXT,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("INSERT INTO `articles_new` SELECT `id`, `name`, `unit`, `totalStock`, `price`, `lastSyncedAt`, `imagePath` FROM `articles`")
                db.execSQL("DROP TABLE `articles`")
                db.execSQL("ALTER TABLE `articles_new` RENAME TO `articles`")
            }
        }

        val MIGRATION_4_5 = object : Migration(4, 5) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // SQLite can't ALTER COLUMN nullability; recreate shelves with storageZoneId nullable.
                db.execSQL("""
                    CREATE TABLE IF NOT EXISTS `shelves_new` (
                        `id` TEXT NOT NULL,
                        `description` TEXT NOT NULL,
                        `storageZoneId` TEXT,
                        PRIMARY KEY(`id`)
                    )
                """)
                db.execSQL("""
                    INSERT INTO `shelves_new` (`id`, `description`, `storageZoneId`)
                    SELECT `id`, `description`, `storageZoneId` FROM `shelves`
                """)
                db.execSQL("DROP TABLE `shelves`")
                db.execSQL("ALTER TABLE `shelves_new` RENAME TO `shelves`")
            }
        }

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
