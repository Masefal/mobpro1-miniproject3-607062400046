package com.masefal_0046.aerovault.model

import com.google.gson.annotations.SerializedName

data class Jet(
    @SerializedName("id") val id: String,
    @SerializedName("nama") val nama: String,
    @SerializedName("asal_negara") val asalNegara: String,
    @SerializedName("role") val role: String,
    @SerializedName("imageUrl") val imageUrl: String
)
