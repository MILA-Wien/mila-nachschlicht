package wien.mila.nachschlichten.ui.capture

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
import wien.mila.nachschlichten.domain.model.Article
import javax.inject.Inject

data class ArticleCheckUiState(
    val article: Article? = null,
    val isLoading: Boolean = true,
    val quantity: Int? = null,
    val saved: Boolean = false,
    val existingPendingItemId: Long? = null
)

@HiltViewModel
class ArticleCheckViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val articleRepository: ArticleRepository,
    private val pendingItemRepository: PendingItemRepository
) : ViewModel() {

    private val ean: String = savedStateHandle["ean"] ?: ""
    private val shelfId: String = savedStateHandle["shelfId"] ?: ""

    private val _uiState = MutableStateFlow(ArticleCheckUiState())
    val uiState: StateFlow<ArticleCheckUiState> = _uiState.asStateFlow()

    init {
        loadArticle()
    }

    private fun loadArticle() {
        viewModelScope.launch {
            val article = articleRepository.getByEan(ean)
            if (article != null) {
                val existing = pendingItemRepository.getByArticleAndShelf(article.id, shelfId)
                _uiState.value = ArticleCheckUiState(
                    article = article,
                    isLoading = false,
                    quantity = existing?.quantity,
                    existingPendingItemId = existing?.id
                )
            } else {
                _uiState.value = ArticleCheckUiState(isLoading = false, saved = true)
            }
        }
    }

    fun incrementQuantity() {
        val current = _uiState.value.quantity
        _uiState.value = _uiState.value.copy(quantity = if (current == null) 1 else current + 1)
    }

    fun decrementQuantity() {
        val current = _uiState.value.quantity ?: return
        _uiState.value = _uiState.value.copy(quantity = if (current <= 1) null else current - 1)
    }

    fun markForRestock() {
        val article = _uiState.value.article ?: return
        viewModelScope.launch {
            val existingId = _uiState.value.existingPendingItemId
            if (existingId != null) {
                pendingItemRepository.updateQuantity(existingId, _uiState.value.quantity)
            } else {
                pendingItemRepository.insert(
                    articleId = article.id,
                    shelfId = shelfId,
                    quantity = _uiState.value.quantity
                )
            }
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }

    fun updateImagePath(path: String) {
        val article = _uiState.value.article ?: return
        viewModelScope.launch {
            articleRepository.updateImagePath(article.id, path)
            _uiState.value = _uiState.value.copy(
                article = article.copy(imagePath = path)
            )
        }
    }

    fun fetchImageIfNeeded() {
        val article = _uiState.value.article ?: return
        if (article.imagePath != null) return
        viewModelScope.launch {
            val url = articleRepository.fetchAndSaveImageFromOpenFoodFacts(article.ean, article.id)
            if (url != null) {
                _uiState.value = _uiState.value.copy(article = article.copy(imagePath = url))
            }
        }
    }
}
