package com.masefal_0046.aerovault.network

import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.model.User
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AeroVaultApiService {

    @GET("jets")
    suspend fun getJets(): Response<List<Jet>>

    @POST("jets")
    suspend fun addJet(@Body jet: Jet): Response<Jet>

    @DELETE("jets/{id}")
    suspend fun deleteJet(@Path("id") id: String): Response<Unit>

    // Dummy login endpoint
    @POST("login")
    suspend fun login(@Body request: Map<String, String>): Response<User>
}
