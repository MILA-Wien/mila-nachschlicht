package wien.mila.nachschlichten.ui.retrieve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.PendingItem
import wien.mila.nachschlichten.domain.model.StorageZone
import javax.inject.Inject

data class ScanResult(
    val pendingItem: PendingItem?,
    val articleName: String?,
    val shelfId: String?,
    val quantity: Double?,
    val notInList: Boolean = false
)

@HiltViewModel
class RetrieveItemListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pendingItemRepository: PendingItemRepository,
    private val storageZoneRepository: StorageZoneRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    private val zoneId: String = savedStateHandle["zoneId"] ?: ""

    val pendingItems: StateFlow<List<PendingItem>> = pendingItemRepository.getByZone(zoneId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _zone = MutableStateFlow<StorageZone?>(null)
    val zone: StateFlow<StorageZone?> = _zone.asStateFlow()

    private val _scanResult = MutableStateFlow<ScanResult?>(null)
    val scanResult: StateFlow<ScanResult?> = _scanResult.asStateFlow()

    init {
        viewModelScope.launch {
            _zone.value = storageZoneRepository.getById(zoneId)
        }
    }

    fun onBarcodeScan(ean: String) {
        viewModelScope.launch {
            val items = pendingItems.value
            val match = items.firstOrNull { it.articleEan == ean && !it.isDone }
            if (match != null) {
                _scanResult.value = ScanResult(
                    pendingItem = match,
                    articleName = match.articleName,
                    shelfId = match.shelfId,
                    quantity = match.quantity
                )
            } else {
                val article = articleRepository.getByEan(ean)
                _scanResult.value = ScanResult(
                    pendingItem = null,
                    articleName = article?.name,
                    shelfId = null,
                    quantity = null,
                    notInList = true
                )
            }
        }
    }

    fun markDone(itemId: Long) {
        viewModelScope.launch {
            pendingItemRepository.markDone(itemId)
            _scanResult.value = null
        }
    }

    fun dismissScanResult() {
        _scanResult.value = null
    }
}
