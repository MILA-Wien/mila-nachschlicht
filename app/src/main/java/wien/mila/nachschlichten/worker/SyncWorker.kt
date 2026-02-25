package wien.mila.nachschlichten.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import wien.mila.nachschlichten.data.datastore.UserPreferences
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.di.CredentialHolder

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val articleRepository: ArticleRepository,
    private val userPreferences: UserPreferences,
    private val credentialHolder: CredentialHolder
) : CoroutineWorker(context, params) {

    companion object {
        const val KEY_ERROR = "error"
        const val KEY_ERROR_DETAIL = "error_detail"
    }

    override suspend fun doWork(): Result {
        val apiUrl = userPreferences.getApiUrlOnce()
        if (apiUrl.isBlank()) {
            return Result.failure(workDataOf(KEY_ERROR to "API URL not set"))
        }

        val url = "${apiUrl.trimEnd('/')}/artikel/"

        return try {
            credentialHolder.username = userPreferences.getUsernameOnce()
            credentialHolder.password = userPreferences.getPasswordOnce()

            val result = articleRepository.syncFromApi(url)
            if (result.isSuccess) {
                userPreferences.setLastSyncedAt(System.currentTimeMillis())
                Result.success()
            } else {
                val e = result.exceptionOrNull()
                val detail = buildString {
                    appendLine("URL: $url")
                    appendLine()
                    append(e?.stackTraceToString() ?: "No stack trace")
                }
                Result.failure(workDataOf(
                    KEY_ERROR to (e?.message ?: "Unknown error"),
                    KEY_ERROR_DETAIL to detail.take(8000)
                ))
            }
        } catch (e: Exception) {
            val detail = buildString {
                appendLine("URL: $url")
                appendLine()
                append(e.stackTraceToString())
            }
            Result.failure(workDataOf(
                KEY_ERROR to e.toString(),
                KEY_ERROR_DETAIL to detail.take(8000)
            ))
        }
    }
}
