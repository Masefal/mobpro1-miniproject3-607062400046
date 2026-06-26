package com.masefal_0046.aerovault.ui.screen

import android.util.Log
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.network.AeroVaultApi
import com.masefal_0046.aerovault.network.AeroVaultApiService
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response

enum class ApiStatus { LOADING, SUCCESS, FAILED }

class MainViewModel(
    private val apiService: AeroVaultApiService
) : ViewModel() {

    var data = mutableStateOf(emptyList<Jet>())
        private set

    var status = MutableStateFlow(ApiStatus.LOADING)
        private set

    var errorMessage = mutableStateOf<String?>(null)
        private set

    fun retrieveData(userId: String = "") {
        viewModelScope.launch(Dispatchers.IO) {
            status.value = ApiStatus.LOADING
            try {
                val response = apiService.getJets()
                if (response.isSuccessful && response.body() != null) {
                    val jets = response.body()!!

                    jets.forEach {
                        Log.d(
                            "CEK_JET",
                            "login=$userId | id=${it.id} | nama=${it.nama} | email=${it.email}"
                        )
                    }

                    data.value = jets
                    status.value = ApiStatus.SUCCESS
                } else {
                    throw Exception(response.toErrorMessage("Gagal mengambil data jet"))
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                status.value = ApiStatus.FAILED
                errorMessage.value = e.message
            }
        }
    }

    fun clearMessage() {
        errorMessage.value = null
    }

    fun saveData(userId: String, name: String, origin: String, role: String, imageBytes: ByteArray) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userId.isBlank()) {
                    throw Exception("Login diperlukan untuk menambah jet")
                }

                val fileName = "jet_${System.currentTimeMillis()}.jpg"
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                val uploadResponse = apiService.uploadImage(fileName, requestFile)

                if (uploadResponse.isSuccessful) {
                    val newJet = Jet(
                        id = null,
                        nama = name,
                        asalNegara = origin,
                        role = role,
                        imageUrl = fileName,
                        email = userId
                    )

                    val postResponse = apiService.postJet(newJet)

                    if (postResponse.isSuccessful) {
                        retrieveData(userId)
                    } else {
                        throw Exception(postResponse.toErrorMessage("Gagal menyimpan detail jet"))
                    }
                } else {
                    throw Exception(uploadResponse.toErrorMessage("Gagal upload gambar jet"))
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }

    fun deleteData(userId: String, jetId: Int) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                if (userId.isBlank()) {
                    throw Exception("Login diperlukan untuk menghapus jet")
                }

                val response = apiService.deleteJet("eq.$jetId", "eq.$userId")
                if (response.isSuccessful) {
                    retrieveData(userId)
                } else {
                    throw Exception(response.toErrorMessage("Gagal menghapus jet"))
                }
            } catch (e: Exception) {
                Log.d("MainViewModel", "Failure: ${e.message}")
                errorMessage.value = "Error: ${e.message}"
            }
        }
    }
}

private fun <T> Response<T>.toErrorMessage(prefix: String): String {
    val errorBody = errorBody()?.string()?.takeIf { it.isNotBlank() }
    return if (errorBody == null) {
        "$prefix (${code()} ${message()})"
    } else {
        "$prefix (${code()}): $errorBody"
    }
}

class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(AeroVaultApi.service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}