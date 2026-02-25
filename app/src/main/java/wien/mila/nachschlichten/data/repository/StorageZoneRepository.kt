package wien.mila.nachschlichten.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wien.mila.nachschlichten.data.local.dao.StorageZoneDao
import wien.mila.nachschlichten.data.local.entity.StorageZoneEntity
import wien.mila.nachschlichten.domain.model.StorageZone
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class StorageZoneRepository @Inject constructor(
    private val storageZoneDao: StorageZoneDao
) {
    fun getAll(): Flow<List<StorageZone>> = storageZoneDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    suspend fun getById(id: String): StorageZone? = storageZoneDao.getById(id)?.toModel()

    suspend fun save(zone: StorageZone) {
        storageZoneDao.upsert(StorageZoneEntity(id = zone.id, description = zone.description, color = zone.color))
    }

    suspend fun delete(id: String) {
        storageZoneDao.delete(id)
    }

    private fun StorageZoneEntity.toModel() = StorageZone(id = id, description = description, color = color)
}
