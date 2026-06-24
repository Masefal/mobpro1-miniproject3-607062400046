package com.masefal_0046.aerovault.network

import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.model.User
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import java.util.UUID

class MockInterceptor : Interceptor {
    private val moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()
        
    private val userAdapter = moshi.adapter(User::class.java)
    private val jetAdapter = moshi.adapter(Jet::class.java)
    private val jetsListAdapter = moshi.adapter<List<Jet>>(Types.newParameterizedType(List::class.java, Jet::class.java))

    // In-memory list to simulate a database
    private val jetsList = mutableListOf(
        Jet(
            id = "1",
            nama = "F-22 Raptor",
            asalNegara = "USA",
            role = "Air Superiority Fighter",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/1/1e/F-22_Raptor_edit1_%28cropped%29.jpg"
        ),
        Jet(
            id = "2",
            nama = "Su-57",
            asalNegara = "Russia",
            role = "Multirole Fighter",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/f/fc/Sukhoi_Su-57_in_flight_in_2020.jpg"
        ),
        Jet(
            id = "3",
            nama = "Eurofighter Typhoon",
            asalNegara = "Multinational (Europe)",
            role = "Multirole Fighter",
            imageUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/c/c9/Royal_Air_Force_Eurofighter_Typhoon.jpg/1200px-Royal_Air_Force_Eurofighter_Typhoon.jpg"
        )
    )

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()
        val uri = request.url.toUri().toString()
        val path = request.url.encodedPath
        
        // Simulate network delay
        runBlocking { delay(1000) }

        var responseString = ""
        var responseCode = 200

        when {
            path.endsWith("/login") && request.method == "POST" -> {
                val user = User(
                    email = "pilot@aerovault.com",
                    name = "Maverick",
                    profilePictureUrl = "https://upload.wikimedia.org/wikipedia/commons/thumb/e/e0/Tom_Cruise_%2834450932580%29.jpg/800px-Tom_Cruise_%2834450932580%29.jpg"
                )
                responseString = userAdapter.toJson(user)
            }
            path.endsWith("/jets") && request.method == "GET" -> {
                responseString = jetsListAdapter.toJson(jetsList)
            }
            path.endsWith("/jets") && request.method == "POST" -> {
                // To extract body for realistic ID generation or error, we simplify here
                // Read from body... but for now just parse body if we can or assume standard
                val buffer = okio.Buffer()
                request.body?.writeTo(buffer)
                val requestBodyString = buffer.readUtf8()
                val newJet = jetAdapter.fromJson(requestBodyString)
                
                if (newJet != null) {
                    val jetWithId = newJet.copy(id = UUID.randomUUID().toString())
                    jetsList.add(jetWithId)
                    responseString = jetAdapter.toJson(jetWithId)
                } else {
                    responseCode = 400
                    responseString = "{\"error\": \"Bad request\"}"
                }
            }
            path.contains("/jets/") && request.method == "DELETE" -> {
                val id = request.url.pathSegments.last()
                val removed = jetsList.removeIf { it.id == id }
                if (removed) {
                    responseString = "{}"
                } else {
                    responseCode = 404
                    responseString = "{\"error\": \"Not found\"}"
                }
            }
            else -> {
                responseCode = 404
                responseString = "{\"error\": \"Not found\"}"
            }
        }

        return Response.Builder()
            .code(responseCode)
            .message("Mock response")
            .request(chain.request())
            .protocol(Protocol.HTTP_1_0)
            .body(responseString.toResponseBody("application/json".toMediaTypeOrNull()))
            .addHeader("content-type", "application/json")
            .build()
    }
}
