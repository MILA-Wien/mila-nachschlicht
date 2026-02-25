package wien.mila.nachschlichten.data.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_preferences")

@Singleton
class UserPreferences @Inject constructor(
    private val context: Context
) {
    private val apiUrlKey = stringPreferencesKey("api_url")
    private val lastSyncedAtKey = longPreferencesKey("last_synced_at")
    private val languageKey = stringPreferencesKey("language")

    val apiUrl: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[apiUrlKey] ?: ""
    }

    val lastSyncedAt: Flow<Long?> = context.dataStore.data.map { prefs ->
        prefs[lastSyncedAtKey]
    }

    val language: Flow<String> = context.dataStore.data.map { prefs ->
        prefs[languageKey] ?: "de"
    }

    suspend fun setApiUrl(url: String) {
        context.dataStore.edit { prefs ->
            prefs[apiUrlKey] = url
        }
    }

    suspend fun setLastSyncedAt(timestamp: Long) {
        context.dataStore.edit { prefs ->
            prefs[lastSyncedAtKey] = timestamp
        }
    }

    suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs ->
            prefs[languageKey] = language
        }
    }

    suspend fun getApiUrlOnce(): String {
        var url = ""
        context.dataStore.edit { prefs ->
            url = prefs[apiUrlKey] ?: ""
        }
        return url
    }
}
