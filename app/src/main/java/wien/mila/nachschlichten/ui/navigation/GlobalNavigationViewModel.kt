package wien.mila.nachschlichten.ui.navigation

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
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.ui.common.BarcodeInputHandler
import wien.mila.nachschlichten.ui.common.HelpScreenKey
import javax.inject.Inject

@HiltViewModel
class GlobalNavigationViewModel @Inject constructor(
    barcodeInputHandler: BarcodeInputHandler,
    shelfRepository: ShelfRepository,
    storageZoneRepository: StorageZoneRepository,
    pendingItemRepository: PendingItemRepository
) : ViewModel() {

    private val shelves = shelfRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    private val zones = storageZoneRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.Eagerly, emptyList())

    val pendingCount: StateFlow<Int> = pendingItemRepository.getPendingCount()
        .stateIn(viewModelScope, SharingStarted.Eagerly, 0)

    private val _navigateToCapture = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToCapture: SharedFlow<String> = _navigateToCapture.asSharedFlow()

    private val _navigateToRetrieve = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val navigateToRetrieve: SharedFlow<String> = _navigateToRetrieve.asSharedFlow()

    private val _unknownShelfId = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val unknownShelfId: SharedFlow<String> = _unknownShelfId.asSharedFlow()

    private val _unknownZoneId = MutableSharedFlow<String>(extraBufferCapacity = 1)
    val unknownZoneId: SharedFlow<String> = _unknownZoneId.asSharedFlow()

    private val _navigateToHelp = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val navigateToHelp: SharedFlow<Unit> = _navigateToHelp.asSharedFlow()

    private val _helpTitle = MutableStateFlow("Hilfe")
    val helpTitle: StateFlow<String> = _helpTitle.asStateFlow()

    private val _helpText = MutableStateFlow("Hier findest du Hilfe zu dieser Seite.")
    val helpText: StateFlow<String> = _helpText.asStateFlow()

    private val _helpKey = MutableStateFlow(HelpScreenKey.DEFAULT)
    val helpKey: StateFlow<String> = _helpKey.asStateFlow()

    private val _helpImageAssetPath = MutableStateFlow<String?>(null)
    val helpImageAssetPath: StateFlow<String?> = _helpImageAssetPath.asStateFlow()

    private val _helpShowGuideButton = MutableStateFlow(false)
    val helpShowGuideButton: StateFlow<Boolean> = _helpShowGuideButton.asStateFlow()

    fun navigateToHelp(
        title: String,
        text: String,
        helpKey: String = HelpScreenKey.DEFAULT,
        imageAssetPath: String? = null,
        showGuideButton: Boolean = false
    ) {
        _helpTitle.value = title
        _helpText.value = text
        _helpKey.value = helpKey
        _helpImageAssetPath.value = imageAssetPath
        _helpShowGuideButton.value = showGuideButton
        _navigateToHelp.tryEmit(Unit)
    }

    init {
        viewModelScope.launch {
            barcodeInputHandler.barcodeFlow.collect { barcode ->
                when {
                    barcode.startsWith("shelf:") -> {
                        val shelfId = barcode.removePrefix("shelf:")
                        if (shelves.value.any { it.id == shelfId })
                            _navigateToCapture.emit(shelfId)
                        else
                            _unknownShelfId.emit(shelfId)
                    }
                    barcode.startsWith("zone:") -> {
                        val zoneId = barcode.removePrefix("zone:")
                        if (zones.value.any { it.id == zoneId })
                            _navigateToRetrieve.emit(zoneId)
                        else
                            _unknownZoneId.emit(zoneId)
                    }
                }
            }
        }
    }
}
