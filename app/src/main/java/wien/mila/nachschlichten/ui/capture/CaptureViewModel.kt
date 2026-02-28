package wien.mila.nachschlichten.ui.capture

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.domain.model.Shelf
import javax.inject.Inject

@HiltViewModel
class CaptureViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shelfRepository: ShelfRepository,
    private val pendingItemRepository: PendingItemRepository,
    private val articleRepository: ArticleRepository
) : ViewModel() {

    val shelfId: String = savedStateHandle["shelfId"] ?: ""

    val shelf: StateFlow<Shelf?> = flow {
        emit(shelfRepository.getById(shelfId))
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val pendingItems = pendingItemRepository.getByShelf(shelfId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _navigateToCheck = MutableSharedFlow<Pair<String, String>>(extraBufferCapacity = 1)
    val navigateToCheck: SharedFlow<Pair<String, String>> = _navigateToCheck.asSharedFlow()

    private val _unknownEan = MutableStateFlow<String?>(null)
    val unknownEan: StateFlow<String?> = _unknownEan.asStateFlow()

    private var unknownEanJob: Job? = null

    fun onBarcodeScan(ean: String) {
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
        viewModelScope.launch {
            pendingItemRepository.deleteAllPendingForShelf(shelfId)
        }
    }
}
