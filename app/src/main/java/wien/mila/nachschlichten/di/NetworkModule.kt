package wien.mila.nachschlichten.di

import android.util.Base64
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import wien.mila.nachschlichten.data.remote.NachschlichtenApi
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideBaseUrlHolder(): BaseUrlHolder = BaseUrlHolder()

    @Provides
    @Singleton
    fun provideCredentialHolder(): CredentialHolder = CredentialHolder()

    @Provides
    @Singleton
    fun provideOkHttpClient(baseUrlHolder: BaseUrlHolder, credentialHolder: CredentialHolder): OkHttpClient {
        val baseUrlInterceptor = Interceptor { chain ->
            val currentUrl = baseUrlHolder.baseUrl
            if (currentUrl.isBlank()) {
                chain.proceed(chain.request())
            } else {
                val originalUrl = chain.request().url
                val newUrl = currentUrl.toHttpUrlOrNull()
                if (newUrl != null) {
                    val rewrittenUrl = originalUrl.newBuilder()
                        .scheme(newUrl.scheme)
                        .host(newUrl.host)
                        .port(newUrl.port)
                        .build()
                    val newRequest = chain.request().newBuilder().url(rewrittenUrl).build()
                    chain.proceed(newRequest)
                } else {
                    chain.proceed(chain.request())
                }
            }
        }

        val authInterceptor = Interceptor { chain ->
            val username = credentialHolder.username
            val password = credentialHolder.password
            val request = if (username.isNotBlank()) {
                val credentials = Base64.encodeToString(
                    "$username:$password".toByteArray(),
                    Base64.NO_WRAP
                )
                chain.request().newBuilder()
                    .header("Authorization", "Basic $credentials")
                    .build()
            } else {
                chain.request()
            }
            chain.proceed(request)
        }

        val logging = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BASIC
        }

        return OkHttpClient.Builder()
            .addInterceptor(baseUrlInterceptor)
            .addInterceptor(authInterceptor)
            .addInterceptor(logging)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(client: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl("http://localhost/")
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideApi(retrofit: Retrofit): NachschlichtenApi {
        return retrofit.create(NachschlichtenApi::class.java)
    }
}

class BaseUrlHolder {
    @Volatile
    var baseUrl: String = ""
}

class CredentialHolder {
    @Volatile
    var username: String = ""
    @Volatile
    var password: String = ""
}
