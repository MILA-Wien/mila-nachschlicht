package wien.mila.nachschlichten.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.Shelf
import wien.mila.nachschlichten.domain.model.StorageZone
import javax.inject.Inject

data class ShelfEditUiState(
    val id: String = "",
    val description: String = "",
    val storageZoneId: String? = null,
    val isNew: Boolean = true,
    val zones: List<StorageZone> = emptyList(),
    val saved: Boolean = false
)

@HiltViewModel
class ShelfEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val shelfRepository: ShelfRepository,
    private val storageZoneRepository: StorageZoneRepository
) : ViewModel() {

    private val shelfId: String? = savedStateHandle["shelfId"]

    private val _uiState = MutableStateFlow(ShelfEditUiState())
    val uiState: StateFlow<ShelfEditUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val zones = storageZoneRepository.getAll().first()
            if (shelfId != null) {
                val shelf = shelfRepository.getById(shelfId)
                if (shelf != null) {
                    _uiState.value = ShelfEditUiState(
                        id = shelf.id,
                        description = shelf.description,
                        storageZoneId = shelf.storageZoneId,
                        isNew = false,
                        zones = zones
                    )
                    return@launch
                }
            }
            _uiState.value = ShelfEditUiState(zones = zones)
        }
    }

    fun updateId(id: String) {
        _uiState.value = _uiState.value.copy(id = id)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateStorageZoneId(zoneId: String?) {
        _uiState.value = _uiState.value.copy(storageZoneId = zoneId)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            shelfRepository.save(Shelf(id = state.id, description = state.description, storageZoneId = state.storageZoneId))
            _uiState.value = state.copy(saved = true)
        }
    }

    fun delete() {
        viewModelScope.launch {
            shelfRepository.delete(_uiState.value.id)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }
}
