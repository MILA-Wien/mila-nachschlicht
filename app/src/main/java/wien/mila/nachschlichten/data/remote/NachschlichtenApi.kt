package wien.mila.nachschlichten.data.remote

import retrofit2.http.GET
import wien.mila.nachschlichten.data.remote.dto.ArticleDto

interface NachschlichtenApi {
    @GET("artikel/")
    suspend fun getArticles(): List<ArticleDto>
}
