package wien.mila.nachschlichten.ui.capture

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
import wien.mila.nachschlichten.domain.model.Shelf
import javax.inject.Inject

data class ShelfWithCount(val shelf: Shelf, val pendingCount: Int)

@HiltViewModel
class CaptureShelfListViewModel @Inject constructor(
    shelfRepository: ShelfRepository,
    pendingItemRepository: PendingItemRepository
) : ViewModel() {
    val shelves: StateFlow<List<ShelfWithCount>> = combine(
        shelfRepository.getAll(),
        pendingItemRepository.getAllPending()
    ) { shelves, pendingItems ->
        val countMap = pendingItems.groupingBy { it.shelfId }.eachCount()
        shelves.map { ShelfWithCount(it, countMap[it.id] ?: 0) }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _invalidScanBarcode = MutableStateFlow<String?>(null)
    val invalidScanBarcode: StateFlow<String?> = _invalidScanBarcode.asStateFlow()

    private val _unknownShelfId = MutableStateFlow<String?>(null)
    val unknownShelfId: StateFlow<String?> = _unknownShelfId.asStateFlow()

    fun onBarcodeScan(barcode: String) {
        if (barcode.startsWith("shelf:")) {
            val shelfId = barcode.removePrefix("shelf:")
            if (!shelves.value.any { it.shelf.id == shelfId }) {
                _unknownShelfId.value = shelfId
            }
            // valid shelf ID: global handler navigates, nothing to do here
        } else {
            _invalidScanBarcode.value = barcode
        }
    }

    fun clearInvalidScan() {
        _invalidScanBarcode.value = null
    }

    fun clearUnknownShelf() {
        _unknownShelfId.value = null
    }
}
