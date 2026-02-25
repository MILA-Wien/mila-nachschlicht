package wien.mila.nachschlichten.data.remote

import retrofit2.http.GET
import retrofit2.http.Url
import wien.mila.nachschlichten.data.remote.dto.ArticleDto

interface NachschlichtenApi {
    @GET
    suspend fun getArticles(@Url url: String): List<ArticleDto>
}
