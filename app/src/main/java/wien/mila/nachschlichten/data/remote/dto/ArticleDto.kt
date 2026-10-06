package wien.mila.nachschlichten.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ArticleDto(
    @SerializedName("ID") val id: Long,
    @SerializedName("barcodes") val eans: List<String>?,
    @SerializedName("name") val name: String,
    @SerializedName("kurzname") val shortName: String,
    @SerializedName("einheit") val unit: String?,
    @SerializedName("lagerstand") val totalStock: Double,
    @SerializedName("vk") val priceNet: Double,
    @SerializedName("mwst") val taxPercentage: Double
)
