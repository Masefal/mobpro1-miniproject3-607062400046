package com.masefal_0046.aerovault.model

import com.squareup.moshi.Json

data class Jet(
    @Json(name = "id")
    val id:  Int? = null,

    @Json(name = "nama")
    val nama: String,

    @Json(name = "asal_negara")
    val asalNegara: String,

    @Json(name = "role")
    val role: String,

    @Json(name = "imageUrl")
    val imageUrl: String
)
