package wien.mila.nachschlichten.ui.retrieve

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.Article
import wien.mila.nachschlichten.domain.model.PendingItem
import wien.mila.nachschlichten.domain.model.StorageZone
import javax.inject.Inject

data class RetrieveItemCheckUiState(
    val article: Article? = null,
    val pendingItem: PendingItem? = null,
    val zone: StorageZone? = null,
    val isLoading: Boolean = true,
    val saved: Boolean = false
)

@HiltViewModel
class RetrieveItemCheckViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val pendingItemRepository: PendingItemRepository,
    private val articleRepository: ArticleRepository,
    private val storageZoneRepository: StorageZoneRepository
) : ViewModel() {

    private val zoneId: String = savedStateHandle["zoneId"] ?: ""
    private val pendingItemId: Long = savedStateHandle["pendingItemId"] ?: 0L

    private val _uiState = MutableStateFlow(RetrieveItemCheckUiState())
    val uiState: StateFlow<RetrieveItemCheckUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val pendingItem = pendingItemRepository.getById(pendingItemId)
            val article = pendingItem?.let { articleRepository.getById(it.articleId) }
            val zone = storageZoneRepository.getById(zoneId)
            _uiState.value = RetrieveItemCheckUiState(
                article = article,
                pendingItem = pendingItem,
                zone = zone,
                isLoading = false
            )
        }
    }

    fun markDone() {
        val pendingItem = _uiState.value.pendingItem ?: return
        viewModelScope.launch {
            pendingItemRepository.markDone(pendingItem.id)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun fetchImageIfNeeded() {
        val article = _uiState.value.article ?: return
        if (article.imagePath != null) return
        viewModelScope.launch {
            val url = articleRepository.fetchAndSaveImageFromOpenFoodFacts(article.eans, article.id)
            if (url != null) {
                _uiState.value = _uiState.value.copy(article = article.copy(imagePath = url))
            }
        }
    }

    fun updateImagePath(path: String) {
        val article = _uiState.value.article ?: return
        viewModelScope.launch {
            articleRepository.updateImagePath(article.id, path)
            _uiState.value = _uiState.value.copy(article = article.copy(imagePath = path))
        }
    }
}
