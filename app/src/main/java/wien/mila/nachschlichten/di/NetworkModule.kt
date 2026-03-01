package wien.mila.nachschlichten.di

import android.util.Base64
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import wien.mila.nachschlichten.data.remote.NachschlichtenApi
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideCredentialHolder(): CredentialHolder = CredentialHolder()

    @Provides
    @Singleton
    @Named("plain")
    fun providePlainOkHttpClient(): OkHttpClient {
        return OkHttpClient.Builder()
            .connectTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .readTimeout(10, java.util.concurrent.TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(credentialHolder: CredentialHolder): OkHttpClient {
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
            .connectTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
            .readTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
            .writeTimeout(5, java.util.concurrent.TimeUnit.MINUTES)
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

class CredentialHolder {
    @Volatile
    var username: String = ""
    @Volatile
    var password: String = ""
}
