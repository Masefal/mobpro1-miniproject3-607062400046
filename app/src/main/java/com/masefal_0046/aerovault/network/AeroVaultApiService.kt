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
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import java.util.concurrent.TimeUnit

private const val BASE_URL = "https://wdprdculwsfajsitlpou.supabase.co/"

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

val okHttpClient = OkHttpClient.Builder()
    .callTimeout(20, TimeUnit.SECONDS)
    .connectTimeout(15, TimeUnit.SECONDS)
    .readTimeout(15, TimeUnit.SECONDS)
    .addInterceptor { chain ->
        val original = chain.request()
        val newRequest = original.newBuilder()
            .header("apikey", BuildConfig.SUPABASE_KEY)
            .header("Authorization", "Bearer ${BuildConfig.SUPABASE_KEY}")
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
    suspend fun getJets(): Response<List<Jet>>

    @POST("storage/v1/object/jet_images/{fileName}")
    suspend fun uploadImage(
        @Path("fileName") fileName: String,
        @Body file: RequestBody
    ): Response<Unit>

    @POST("rest/v1/jetsx")
    suspend fun postJet(
        @Body jet: Jet
    ): Response<Unit>

    @DELETE("rest/v1/jetsx")
    suspend fun deleteJet(
        @Query("id") id: String,
        @Query("email") email: String
    ): Response<Unit>
}

object AeroVaultApi {
    val service: AeroVaultApiService by lazy {
        retrofit.create(AeroVaultApiService::class.java)
    }

    fun getJetUrl(imageId: String): String {
        if (imageId.startsWith("http://") || imageId.startsWith("https://")) {
            return imageId
        }
        return "${BASE_URL}storage/v1/object/public/jet_images/$imageId"
    }
}
