package com.masefal_0046.aerovault.network

import com.masefal_0046.aerovault.BuildConfig
import com.masefal_0046.aerovault.model.Jet
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

private const val BASE_URL = "https://wdprdculwsfajsitlpou.supabase.co/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val okHttpClient = OkHttpClient.Builder()
    .addInterceptor { chain ->
        val original = chain.request()
        val newRequest = original.newBuilder()
            .addHeader("apikey", BuildConfig.SUPABASE_KEY)
            .addHeader("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
            .build()
        chain.proceed(newRequest)
    }
    .build()

private val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .baseUrl(BASE_URL)
    .build()

interface AeroVaultApiService {
    @GET("rest/v1/jetsx?select=*")
    suspend fun getJets(
        @Header("Authorization") token: String
    ): Response<List<Jet>>

    @POST("storage/v1/object/jet_images/{fileName}")
    suspend fun uploadImage(
        @Header("Authorization") token: String,
        @Path("fileName") fileName: String,
        @Body file: RequestBody
    ): Response<Unit>

    @POST("rest/v1/jetsx")
    suspend fun postJet(
        @Header("Authorization") token: String,
        @Body jet: Jet
    ): Response<Unit>

    @DELETE("rest/v1/jetsx")
    suspend fun deleteJet(
        @Header("Authorization") token: String,
        @Query("id") id: String
    ): Response<Unit>
}

object AeroVaultApi {
    val service: AeroVaultApiService by lazy {
        retrofit.create(AeroVaultApiService::class.java)
    }

    fun getJetUrl(imageId: String): String {
        return "${BASE_URL}storage/v1/object/public/jet_images/$imageId"
    }
}