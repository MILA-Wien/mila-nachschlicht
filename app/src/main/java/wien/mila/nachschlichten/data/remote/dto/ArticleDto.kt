package wien.mila.nachschlichten.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArticleDto(
    @SerializedName("id") val id: Long,
    @SerializedName("ean") val ean: String,
    @SerializedName("name") val name: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("total_stock") val totalStock: Int,
    @SerializedName("price") val price: Double
)
