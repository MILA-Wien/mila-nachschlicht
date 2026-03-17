package wien.mila.nachschlichten.ui.retrieve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.PendingItem
import wien.mila.nachschlichten.domain.model.StorageZone
import wien.mila.nachschlichten.ui.common.TimedErrorState
import javax.inject.Inject

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

    private val _navigateToCheck = MutableSharedFlow<Long>()
    val navigateToCheck: SharedFlow<Long> = _navigateToCheck.asSharedFlow()

    private val timedNotInList = TimedErrorState(viewModelScope)
    val notInListEan: StateFlow<String?> = timedNotInList.value

    init {
        viewModelScope.launch {
            _zone.value = storageZoneRepository.getById(zoneId)
        }
    }

    fun markDone(id: Long) {
        viewModelScope.launch { pendingItemRepository.markDone(id) }
    }

    fun unmarkDone(id: Long) {
        viewModelScope.launch { pendingItemRepository.unmarkDone(id) }
    }

    fun onBarcodeScan(ean: String) {
        viewModelScope.launch {
            val items = pendingItems.value
            val match = items.firstOrNull { ean in it.articleEans && !it.isDone }
            if (match != null) {
                _navigateToCheck.emit(match.id)
            } else {
                timedNotInList.show(ean)
            }
        }
    }
}
