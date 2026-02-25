package wien.mila.nachschlichten.worker

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import wien.mila.nachschlichten.data.datastore.UserPreferences
import wien.mila.nachschlichten.data.repository.ArticleRepository
import wien.mila.nachschlichten.di.BaseUrlHolder
import wien.mila.nachschlichten.di.CredentialHolder

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val articleRepository: ArticleRepository,
    private val userPreferences: UserPreferences,
    private val baseUrlHolder: BaseUrlHolder,
    private val credentialHolder: CredentialHolder
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val apiUrl = userPreferences.getApiUrlOnce()
        if (apiUrl.isBlank()) return Result.failure()

        baseUrlHolder.baseUrl = apiUrl
        credentialHolder.username = userPreferences.getUsernameOnce()
        credentialHolder.password = userPreferences.getPasswordOnce()

        val result = articleRepository.syncFromApi()
        return if (result.isSuccess) {
            userPreferences.setLastSyncedAt(System.currentTimeMillis())
            Result.success()
        } else {
            if (runAttemptCount < 3) Result.retry()
            else Result.failure()
        }
    }
}
