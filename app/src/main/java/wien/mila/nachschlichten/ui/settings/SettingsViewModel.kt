package wien.mila.nachschlichten.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import wien.mila.nachschlichten.data.datastore.UserPreferences
import wien.mila.nachschlichten.data.repository.PendingItemRepository
import wien.mila.nachschlichten.data.repository.ShelfRepository
import wien.mila.nachschlichten.data.repository.StorageZoneRepository
import wien.mila.nachschlichten.domain.model.Shelf
import wien.mila.nachschlichten.domain.model.StorageZone
import wien.mila.nachschlichten.worker.SyncWorker
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val userPreferences: UserPreferences,
    private val shelfRepository: ShelfRepository,
    private val storageZoneRepository: StorageZoneRepository,
    private val pendingItemRepository: PendingItemRepository,
    private val workManager: WorkManager
) : ViewModel() {

    val apiUrl: StateFlow<String> = userPreferences.apiUrl
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val username: StateFlow<String> = userPreferences.username
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val password: StateFlow<String> = userPreferences.password
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "")

    val lastSyncedAt: StateFlow<Long?> = userPreferences.lastSyncedAt
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    val language: StateFlow<String> = userPreferences.language
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), "de")

    val shelves: StateFlow<List<Shelf>> = shelfRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val zones: StateFlow<List<StorageZone>> = storageZoneRepository.getAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private val _isSyncing = MutableStateFlow(false)
    val isSyncing: StateFlow<Boolean> = _isSyncing.asStateFlow()

    fun updateApiUrl(url: String) {
        viewModelScope.launch {
            userPreferences.setApiUrl(url)
        }
    }

    fun updateUsername(username: String) {
        viewModelScope.launch {
            userPreferences.setUsername(username)
        }
    }

    fun updatePassword(password: String) {
        viewModelScope.launch {
            userPreferences.setPassword(password)
        }
    }

    fun triggerSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>().build()
        workManager.enqueueUniqueWork(
            "manual_sync",
            ExistingWorkPolicy.REPLACE,
            request
        )
        viewModelScope.launch {
            workManager.getWorkInfoByIdFlow(request.id).collect { info ->
                when (info?.state) {
                    WorkInfo.State.RUNNING -> _isSyncing.value = true
                    WorkInfo.State.SUCCEEDED, WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> {
                        _isSyncing.value = false
                    }
                    else -> {}
                }
            }
        }
    }

    fun resetPending() {
        viewModelScope.launch {
            pendingItemRepository.deleteAllPending()
        }
    }

    fun setLanguage(lang: String) {
        viewModelScope.launch {
            userPreferences.setLanguage(lang)
            val localeList = LocaleListCompat.forLanguageTags(lang)
            AppCompatDelegate.setApplicationLocales(localeList)
        }
    }

    fun deleteShelf(id: String) {
        viewModelScope.launch {
            shelfRepository.delete(id)
        }
    }

    fun deleteZone(id: String) {
        viewModelScope.launch {
            storageZoneRepository.delete(id)
        }
    }
}
