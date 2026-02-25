package wien.mila.nachschlichten.ui.retrieve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.ProductGroup
import javax.inject.Inject

@HiltViewModel
class RetrieveViewModel @Inject constructor(
    storageZoneRepository: StorageZoneRepository,
    shelfRepository: ShelfRepository,
    pendingItemRepository: PendingItemRepository
) : ViewModel() {

    val productGroups: StateFlow<List<ProductGroup>> = combine(
        storageZoneRepository.getAll(),
        shelfRepository.getAll(),
        pendingItemRepository.getPendingCountByZone()
    ) { zones, shelves, counts ->
        val countMap = counts.associate { it.storageZoneId to it.pendingCount }
        zones.map { zone ->
            ProductGroup(
                zone = zone,
                pendingCount = countMap[zone.id] ?: 0,
                shelves = shelves.filter { it.storageZoneId == zone.id }
            )
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPending: StateFlow<Int> = pendingItemRepository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDone: StateFlow<Int> = pendingItemRepository.getDoneCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)
}
