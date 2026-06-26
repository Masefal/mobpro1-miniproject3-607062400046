package com.masefal_0046.aerovault.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.network.AeroVaultApi
import com.masefal_0046.aerovault.network.AeroVaultApiService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody

sealed class NetworkResult<out T> {
    object Idle : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

class MainViewModel(
    private val apiService: AeroVaultApiService
) : ViewModel() {

    val errorMessage = androidx.compose.runtime.mutableStateOf<String?>(null)

    private val _jetsState = MutableStateFlow<NetworkResult<List<Jet>>>(NetworkResult.Idle)
    val jetsState: StateFlow<NetworkResult<List<Jet>>> = _jetsState.asStateFlow()

    private val _addJetState = MutableStateFlow<NetworkResult<Jet>>(NetworkResult.Idle)
    val addJetState: StateFlow<NetworkResult<Jet>> = _addJetState.asStateFlow()

    private val _deleteJetState = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.Idle)
    val deleteJetState: StateFlow<NetworkResult<Unit>> = _deleteJetState.asStateFlow()


    fun fetchJets(token: String) {
        viewModelScope.launch {
            _jetsState.value = NetworkResult.Loading
            try {
                val response = apiService.getJets(token)
                if (response.isSuccessful && response.body() != null) {
                    _jetsState.value = NetworkResult.Success(response.body()!!)
                } else {
                    _jetsState.value = NetworkResult.Error("Failed to fetch data: ${response.message()}")
                }
            } catch (e: Exception) {
                _jetsState.value = NetworkResult.Error("No Internet Connection or Server Error")
            }
        }
    }

    fun addJet(token: String, name: String, origin: String, role: String, imageBytes: ByteArray) {
        viewModelScope.launch {
            _addJetState.value = NetworkResult.Loading
            try {
                val fileName = "jet_${System.currentTimeMillis()}.jpg"
                val requestFile = imageBytes.toRequestBody("image/jpeg".toMediaTypeOrNull())

                val uploadResponse = apiService.uploadImage(token, fileName, requestFile)

                if (uploadResponse.isSuccessful) {
                    val newJet = Jet(
                        id = null,
                        nama = name,
                        asalNegara = origin,
                        role = role,
                        imageUrl = fileName
                    )

                    val postResponse = apiService.postJet(token, newJet)

                    if (postResponse.isSuccessful) {
                        _addJetState.value = NetworkResult.Success(newJet)
                        fetchJets(token)
                    } else {
                        _addJetState.value = NetworkResult.Error("Gagal menyimpan detail jet")
                    }
                } else {
                    _addJetState.value = NetworkResult.Error("Gagal upload gambar jet")
                }
            } catch (e: Exception) {
                _addJetState.value = NetworkResult.Error("Network Error: ${e.localizedMessage}")
            }
        }
    }

    fun deleteJet(token: String, id: Int) {
        viewModelScope.launch {
            _deleteJetState.value = NetworkResult.Loading
            try {
                val response = apiService.deleteJet(token, "eq.$id")
                if (response.isSuccessful) {
                    _deleteJetState.value = NetworkResult.Success(Unit)
                    fetchJets(token)
                } else {
                    _deleteJetState.value = NetworkResult.Error("Failed to delete jet")
                }
            } catch (e: Exception) {
                _deleteJetState.value = NetworkResult.Error("Network Error")
            }
        }
    }

    fun resetAddJetState() {
        _addJetState.value = NetworkResult.Idle
    }

    fun resetDeleteJetState() {
        _deleteJetState.value = NetworkResult.Idle
    }
}

// Factory diperbaiki agar mengambil apiService secara otomatis
class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            // Mengambil instance singleton AeroVaultApi.service yang udah lo buat
            return MainViewModel(AeroVaultApi.service) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}