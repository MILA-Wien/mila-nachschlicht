package wien.mila.nachschlichten.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import wien.mila.nachschlichten.data.local.dao.ShelfDao
import wien.mila.nachschlichten.data.local.entity.ShelfEntity
import wien.mila.nachschlichten.domain.model.Shelf
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ShelfRepository @Inject constructor(
    private val shelfDao: ShelfDao
) {
    fun getAll(): Flow<List<Shelf>> = shelfDao.getAll().map { list ->
        list.map { it.toModel() }
    }

    fun getByZone(zoneId: String): Flow<List<Shelf>> = shelfDao.getByZone(zoneId).map { list ->
        list.map { it.toModel() }
    }

    suspend fun getById(id: String): Shelf? = shelfDao.getById(id)?.toModel()

    suspend fun save(shelf: Shelf) {
        shelfDao.upsert(ShelfEntity(id = shelf.id, name = shelf.name, storageZoneId = shelf.storageZoneId))
    }

    suspend fun delete(id: String) {
        shelfDao.delete(id)
    }

    private fun ShelfEntity.toModel() = Shelf(id = id, name = name, storageZoneId = storageZoneId)
}
