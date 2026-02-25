package wien.mila.nachschlichten.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.domain.model.PendingItem
import wien.mila.nachschlichten.domain.model.Shelf
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val pendingItemRepository: PendingItemRepository
) : ViewModel() {

    val shelves: StateFlow<List<Shelf>> = shelfRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _selectedShelfId = MutableStateFlow<String?>(null)
    val selectedShelfId: StateFlow<String?> = _selectedShelfId.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    val pendingItems: StateFlow<List<PendingItem>> = _selectedShelfId
        .flatMapLatest { shelfId ->
            if (shelfId != null) pendingItemRepository.getByShelf(shelfId)
            else flowOf(emptyList())
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun selectShelf(shelfId: String) {
        _selectedShelfId.value = shelfId
    }

    fun deleteAllPendingForShelf() {
        val shelfId = _selectedShelfId.value ?: return
        viewModelScope.launch {
            pendingItemRepository.deleteAllPendingForShelf(shelfId)
        }
    }
}
