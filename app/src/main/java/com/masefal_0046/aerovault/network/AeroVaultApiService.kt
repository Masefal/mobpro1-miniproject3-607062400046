package com.masefal_0046.aerovault.network

import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.model.OpStatus
import com.masefal_0046.aerovault.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Query

private const val BASE_URL = "https://wdprdculwsfajsitlpou.supabase.co/"

private const val API_KEY = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndkcHJkY3Vsd3NmYWpzaXRscG91Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyODM3MDQsImV4cCI6MjA5Nzg1OTcwNH0.HjunHHQg-L9zQAINksSvzNKH4d4k0uhC3K4qjVl08Fg"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(com.masefal_0046.aerovault.network.BASE_URL)
    .build()

interface AeroVaultApiService {

    @GET("jetsx")
    suspend fun getJets(
        @Header("Authorization") userId: String
    ): Response<List<Jet>>

    @Multipart
    @POST("jetsx")
    suspend fun postJet(
        @Header("Authorization") userId: String,
        @Part("nama") nama: RequestBody,
        @Part("asal_negara") asalNegara: RequestBody,
        @Part("role") role: RequestBody,
        @Part image: MultipartBody.Part,
    ): OpStatus

    @DELETE("jetsx/{id}")
    suspend fun deleteJet(
        @Header("Authorization") userId: String,
        @Query("id") id: String
    ): OpStatus

    // Dummy login endpoint
    @POST("login")
    suspend fun login(@Body request: Map<String, String>): Response<User>
}
