package com.masefal_0046.aerovault.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    
    private const val BASE_URL = "https://api.aerovault.com/" // Dummy URL, MockInterceptor intercepts it

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    private val mockInterceptor = MockInterceptor()

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(mockInterceptor)
        .build()

    val apiService: AeroVaultApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
            .create(AeroVaultApiService::class.java)
    }
}
