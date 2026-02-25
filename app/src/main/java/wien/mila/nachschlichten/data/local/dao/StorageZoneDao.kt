package wien.mila.nachschlichten.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wien.mila.nachschlichten.data.local.entity.StorageZoneEntity

@Dao
interface StorageZoneDao {
    @Query("SELECT * FROM storage_zones ORDER BY id")
    fun getAll(): Flow<List<StorageZoneEntity>>

    @Query("SELECT * FROM storage_zones WHERE id = :id")
    suspend fun getById(id: String): StorageZoneEntity?

    @Upsert
    suspend fun upsert(zone: StorageZoneEntity)

    @Query("DELETE FROM storage_zones WHERE id = :id")
    suspend fun delete(id: String)
}
