package com.masefal_0046.aerovault.network

import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory

object NetworkModule {
    
    // Use the Supabase URL
    private const val BASE_URL = "https://wdprdculwsfajsitlpou.supabase.co/"

    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // Real interceptor to attach Supabase API Key
    private val authInterceptor = okhttp3.Interceptor { chain ->
        val req = chain.request()
        val newReq = req.newBuilder()
            .addHeader("apikey", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6IndkcHJkY3Vsd3NmYWpzaXRscG91Iiwicm9sZSI6ImFub24iLCJpYXQiOjE3ODIyODM3MDQsImV4cCI6MjA5Nzg1OTcwNH0.HjunHHQg-L9zQAINksSvzNKH4d4k0uhC3K4qjVl08Fg")
            .build()
        chain.proceed(newReq)
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(authInterceptor)
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
