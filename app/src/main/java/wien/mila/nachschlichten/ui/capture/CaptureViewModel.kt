package wien.mila.nachschlichten.ui.capture

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.domain.model.PendingItem
import wien.mila.nachschlichten.domain.model.Shelf
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    private val shelfRepository: ShelfRepository,
    private val pendingItemRepository: PendingItemRepository,
    private val articleRepository: ArticleRepository
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

    private val _navigateToCheck = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val navigateToCheck: SharedFlow<Pair<String, String>> = _navigateToCheck.asSharedFlow()

    private val _unknownEan = MutableStateFlow<String?>(null)
    val unknownEan: StateFlow<String?> = _unknownEan.asStateFlow()

    private var unknownEanJob: Job? = null

    fun selectShelf(shelfId: String) {
        _selectedShelfId.value = shelfId
    }

    fun onBarcodeScan(ean: String) {
        val shelfId = _selectedShelfId.value ?: return
        viewModelScope.launch {
            val article = articleRepository.getByEan(ean)
            if (article != null) {
                _unknownEan.value = null
                unknownEanJob?.cancel()
                _navigateToCheck.emit(ean to shelfId)
            } else {
                unknownEanJob?.cancel()
                _unknownEan.value = ean
                unknownEanJob = launch {
                    delay(3000)
                    _unknownEan.value = null
                }
            }
        }
    }

    fun deleteAllPendingForShelf() {
        val shelfId = _selectedShelfId.value ?: return
        viewModelScope.launch {
            pendingItemRepository.deleteAllPendingForShelf(shelfId)
        }
    }
}
