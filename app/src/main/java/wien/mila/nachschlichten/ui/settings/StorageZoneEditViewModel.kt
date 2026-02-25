package wien.mila.nachschlichten.ui.settings

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.StorageZone
import javax.inject.Inject

data class StorageZoneEditUiState(
    val id: String = "",
    val description: String = "",
    val color: String = "#0066CC",
    val isNew: Boolean = true,
    val saved: Boolean = false
)

@HiltViewModel
class StorageZoneEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val storageZoneRepository: StorageZoneRepository
) : ViewModel() {

    private val zoneId: String? = savedStateHandle["zoneId"]

    private val _uiState = MutableStateFlow(StorageZoneEditUiState())
    val uiState: StateFlow<StorageZoneEditUiState> = _uiState.asStateFlow()

    init {
        if (zoneId != null) {
            viewModelScope.launch {
                val zone = storageZoneRepository.getById(zoneId)
                if (zone != null) {
                    _uiState.value = StorageZoneEditUiState(
                        id = zone.id,
                        description = zone.description,
                        color = zone.color,
                        isNew = false
                    )
                }
            }
        }
    }

    fun updateId(id: String) {
        _uiState.value = _uiState.value.copy(id = id)
    }

    fun updateDescription(description: String) {
        _uiState.value = _uiState.value.copy(description = description)
    }

    fun updateColor(color: String) {
        _uiState.value = _uiState.value.copy(color = color)
    }

    fun save() {
        val state = _uiState.value
        viewModelScope.launch {
            storageZoneRepository.save(StorageZone(id = state.id, description = state.description, color = state.color))
            _uiState.value = state.copy(saved = true)
        }
    }

    fun delete() {
        viewModelScope.launch {
            storageZoneRepository.delete(_uiState.value.id)
            _uiState.value = _uiState.value.copy(saved = true)
        }
    }
}
