package wien.mila.nachschlichten.data.local.dao

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow
import wien.mila.nachschlichten.data.local.entity.ShelfEntity

@Dao
interface ShelfDao {
    @Query("SELECT * FROM shelves ORDER BY id")
    fun getAll(): Flow<List<ShelfEntity>>

    @Query("SELECT * FROM shelves WHERE storageZoneId = :zoneId ORDER BY id")
    fun getByZone(zoneId: String): Flow<List<ShelfEntity>>

    @Query("SELECT * FROM shelves WHERE id = :id")
    suspend fun getById(id: String): ShelfEntity?

    @Upsert
    suspend fun upsert(shelf: ShelfEntity)

    @Query("DELETE FROM shelves WHERE id = :id")
    suspend fun delete(id: String)
}
