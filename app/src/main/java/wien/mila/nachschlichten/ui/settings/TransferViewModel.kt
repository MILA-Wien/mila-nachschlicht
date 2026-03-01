package wien.mila.nachschlichten.ui.settings

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.transfer.TransferFile
import wien.mila.nachschlichten.data.transfer.TransferOptions
import wien.mila.nachschlichten.data.transfer.TransferRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

sealed class TransferSideEffect {
    data class LaunchExportFilePicker(val suggestedName: String) : TransferSideEffect()
    object LaunchImportFilePicker : TransferSideEffect()
}

data class TransferUiState(
    val isWorking: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null,
    val pendingImportFile: TransferFile? = null,
    val exportOptions: TransferOptions = TransferOptions(),
    val importOptions: TransferOptions = TransferOptions(),
    val importZonesShelvesError: String? = null
)

@HiltViewModel
class TransferViewModel @Inject constructor(
    private val transferRepository: TransferRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(TransferUiState())
    val uiState = _uiState.asStateFlow()

    private val _sideEffects = Channel<TransferSideEffect>(Channel.BUFFERED)
    val sideEffects = _sideEffects.receiveAsFlow()

    private val gson = Gson()

    fun setExportOptions(options: TransferOptions) {
        _uiState.update { it.copy(exportOptions = options) }
    }

    fun setImportOptions(options: TransferOptions) {
        _uiState.update { it.copy(importOptions = options) }
    }

    fun clearMessages() {
        _uiState.update { it.copy(errorMessage = null, successMessage = null) }
    }

    fun requestExport() {
        val timestamp = SimpleDateFormat("yyyyMMdd-HHmmss", Locale.getDefault()).format(Date())
        viewModelScope.launch {
            _sideEffects.send(TransferSideEffect.LaunchExportFilePicker("mila-export-$timestamp.json"))
        }
    }

    fun onExportUriSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            try {
                val export = transferRepository.buildExport(_uiState.value.exportOptions)
                val json = gson.toJson(export)
                context.contentResolver.openOutputStream(uri)?.use { stream ->
                    stream.write(json.toByteArray())
                }
                _uiState.update { it.copy(isWorking = false, successMessage = "export_success") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isWorking = false, errorMessage = e.message ?: "error") }
            }
        }
    }

    fun requestImport() {
        viewModelScope.launch {
            _sideEffects.send(TransferSideEffect.LaunchImportFilePicker)
        }
    }

    fun onImportUriSelected(uri: Uri, context: Context) {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, errorMessage = null, successMessage = null) }
            try {
                val json = context.contentResolver.openInputStream(uri)?.use { stream ->
                    stream.readBytes().toString(Charsets.UTF_8)
                } ?: throw IllegalStateException("Could not read file")

                val transferFile = gson.fromJson(json, TransferFile::class.java)
                    ?: throw IllegalStateException("parse_error")

                if (transferFile.version != 1) {
                    _uiState.update { it.copy(isWorking = false, errorMessage = "version_mismatch:${transferFile.version}") }
                    return@launch
                }

                // Kotlin non-nullable fields can be null at runtime when set by Gson
                @Suppress("SENSELESS_COMPARISON")
                val invalidShelves = transferFile.shelves?.count {
                    it.id == null || it.description == null
                } ?: 0

                @Suppress("SENSELESS_COMPARISON")
                val invalidZones = transferFile.zones?.count {
                    it.id == null || it.description == null || it.color == null
                } ?: 0

                val zonesShelvesError: String? = when {
                    invalidShelves > 0 -> "shelves_missing_field:$invalidShelves"
                    invalidZones > 0 -> "zones_missing_field:$invalidZones"
                    else -> null
                }

                // Preset import options based on what's in the file
                val presetOptions = TransferOptions(
                    includeApiSettings = transferFile.apiSettings != null,
                    includeZonesAndShelves = (transferFile.zones != null || transferFile.shelves != null)
                                             && zonesShelvesError == null,
                    includeArticleImages = transferFile.articleImages != null,
                    includePendingItems = transferFile.pendingItems != null
                )

                _uiState.update { it.copy(
                    isWorking = false,
                    pendingImportFile = transferFile,
                    importOptions = presetOptions,
                    importZonesShelvesError = zonesShelvesError
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isWorking = false, errorMessage = "parse_error") }
            }
        }
    }

    fun onImportOptionsConfirmed() {
        val file = _uiState.value.pendingImportFile ?: return
        val options = _uiState.value.importOptions
        viewModelScope.launch {
            _uiState.update { it.copy(isWorking = true, pendingImportFile = null) }
            try {
                val result = transferRepository.applyImport(file, options)
                _uiState.update { it.copy(
                    isWorking = false,
                    successMessage = "import_success:${result.skippedPendingItems}"
                ) }
            } catch (e: Exception) {
                _uiState.update { it.copy(isWorking = false, errorMessage = e.message ?: "error") }
            }
        }
    }

    fun onImportCancelled() {
        _uiState.update { it.copy(pendingImportFile = null, importZonesShelvesError = null) }
    }
}
