package wien.mila.nachschlichten.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import wien.mila.nachschlichten.data.local.NachschlichtenDatabase
import wien.mila.nachschlichten.data.local.dao.ArticleDao
import wien.mila.nachschlichten.data.local.dao.PendingItemDao
import wien.mila.nachschlichten.data.local.dao.ShelfDao
import wien.mila.nachschlichten.data.local.dao.StorageZoneDao
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): NachschlichtenDatabase {
        return Room.databaseBuilder(
            context,
            NachschlichtenDatabase::class.java,
            "nachschlichten.db"
        ).addMigrations(NachschlichtenDatabase.MIGRATION_3_4, NachschlichtenDatabase.MIGRATION_4_5).build()
    }

    @Provides
    fun provideShelfDao(db: NachschlichtenDatabase): ShelfDao = db.shelfDao()

    @Provides
    fun provideStorageZoneDao(db: NachschlichtenDatabase): StorageZoneDao = db.storageZoneDao()

    @Provides
    fun provideArticleDao(db: NachschlichtenDatabase): ArticleDao = db.articleDao()

    @Provides
    fun providePendingItemDao(db: NachschlichtenDatabase): PendingItemDao = db.pendingItemDao()
}
