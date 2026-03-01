package wien.mila.nachschlichten.ui.retrieve

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
        val zoneGroups = zones.map { zone ->
            ProductGroup(
                zone = zone,
                pendingCount = countMap[zone.id] ?: 0,
                shelves = shelves.filter { it.storageZoneId == zone.id }
            )
        }
        val noZoneShelves = shelves.filter { it.storageZoneId == null }
        val noZoneGroup = if (noZoneShelves.isNotEmpty())
            listOf(ProductGroup(zone = null, pendingCount = countMap[null] ?: 0, shelves = noZoneShelves))
        else emptyList()
        zoneGroups + noZoneGroup
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val totalPending: StateFlow<Int> = pendingItemRepository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalDone: StateFlow<Int> = pendingItemRepository.getDoneCount()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    private val _invalidScanBarcode = MutableStateFlow<String?>(null)
    val invalidScanBarcode: StateFlow<String?> = _invalidScanBarcode.asStateFlow()

    private val _unknownZoneId = MutableStateFlow<String?>(null)
    val unknownZoneId: StateFlow<String?> = _unknownZoneId.asStateFlow()

    fun onBarcodeScan(barcode: String) {
        if (barcode.startsWith("zone:")) {
            val zoneId = barcode.removePrefix("zone:")
            if (!productGroups.value.any { it.zone?.id == zoneId }) {
                _unknownZoneId.value = zoneId
            }
            // valid zone ID: global handler navigates, nothing to do here
        } else {
            _invalidScanBarcode.value = barcode
        }
    }

    fun clearInvalidScan() {
        _invalidScanBarcode.value = null
    }

    fun clearUnknownZone() {
        _unknownZoneId.value = null
    }
}
