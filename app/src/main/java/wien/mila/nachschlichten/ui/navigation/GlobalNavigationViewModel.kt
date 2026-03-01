package wien.mila.nachschlichten.ui.navigation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.ui.common.BarcodeInputHandler
import javax.inject.Inject

@HiltViewModel
class GlobalNavigationViewModel @Inject constructor(
    barcodeInputHandler: BarcodeInputHandler,
    shelfRepository: ShelfRepository,
    storageZoneRepository: StorageZoneRepository,
    pendingItemRepository: PendingItemRepository
) : ViewModel() {

    private val shelves = shelfRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val zones = storageZoneRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pendingCount: StateFlow<Int> = pendingItemRepository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _navigateToCapture = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToCapture: SharedFlow<String> = _navigateToCapture.asSharedFlow()

    private val _navigateToRetrieve = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToRetrieve: SharedFlow<String> = _navigateToRetrieve.asSharedFlow()

    private val _unknownShelfId = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val unknownShelfId: SharedFlow<String> = _unknownShelfId.asSharedFlow()

    private val _unknownZoneId = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val unknownZoneId: SharedFlow<String> = _unknownZoneId.asSharedFlow()

    init {
        viewModelScope.launch {
            barcodeInputHandler.barcodeFlow.collect { barcode ->
                when {
                    barcode.startsWith("shelf:") -> {
                        val shelfId = barcode.removePrefix("shelf:")
                        if (shelves.value.any { it.id == shelfId })
                            _navigateToCapture.emit(shelfId)
                        else
                            _unknownShelfId.emit(shelfId)
                    }
                    barcode.startsWith("zone:") -> {
                        val zoneId = barcode.removePrefix("zone:")
                        if (zones.value.any { it.id == zoneId })
                            _navigateToRetrieve.emit(zoneId)
                        else
                            _unknownZoneId.emit(zoneId)
                    }
                }
            }
        }
    }
}
