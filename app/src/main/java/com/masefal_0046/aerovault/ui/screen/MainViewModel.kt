package com.masefal_0046.aerovault.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.masefal_0046.aerovault.network.UserDataStore
import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.model.User
import com.masefal_0046.aerovault.network.AeroVaultApiService
import com.masefal_0046.aerovault.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody

sealed class NetworkResult<out T> {
    object Idle : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

class MainViewModel(
    private val apiService: AeroVaultApiService = NetworkModule.apiService
) : ViewModel() {

    val errorMessage = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val _jetsState = MutableStateFlow<NetworkResult<List<Jet>>>(NetworkResult.Idle)
    val jetsState: StateFlow<NetworkResult<List<Jet>>> = _jetsState.asStateFlow()

    private val _addJetState = MutableStateFlow<NetworkResult<Jet>>(NetworkResult.Idle)
    val addJetState: StateFlow<NetworkResult<Jet>> = _addJetState.asStateFlow()

    private val _deleteJetState = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.Idle)
    val deleteJetState: StateFlow<NetworkResult<Unit>> = _deleteJetState.asStateFlow()



    fun fetchJets(email: String) {
        viewModelScope.launch {
            _jetsState.value = NetworkResult.Loading
            try {
                val response = apiService.getJets(email)
                if (response.isSuccessful && response.body() != null) {
                    _jetsState.value = NetworkResult.Success(response.body()!!)
                } else {
                    _jetsState.value = NetworkResult.Error("Failed to fetch data")
                }
            } catch (e: Exception) {
                _jetsState.value = NetworkResult.Error("No Internet Connection or Server Error")
            }
        }
    }

    fun addJet(email: String, name: String, origin: String, role: String, imageUrl: String) {
        viewModelScope.launch {
            _addJetState.value = NetworkResult.Loading
            try {
                val newJet = Jet(id = "", nama = name, asalNegara = origin, role = role, imageUrl = imageUrl)
                
                val namaBody = name.toRequestBody("text/plain".toMediaTypeOrNull())
                val asalNegaraBody = origin.toRequestBody("text/plain".toMediaTypeOrNull())
                val roleBody = role.toRequestBody("text/plain".toMediaTypeOrNull())
                val dummyImage = MultipartBody.Part.createFormData("image", "dummy.jpg", "dummy".toRequestBody("image/jpeg".toMediaTypeOrNull()))
                
                val status = apiService.postJet(email, namaBody, asalNegaraBody, roleBody, dummyImage)
                if (status.status == "success") {
                    _addJetState.value = NetworkResult.Success(newJet)
                    fetchJets(email) // refresh list
                } else {
                    _addJetState.value = NetworkResult.Error("Failed to add jet")
                }
            } catch (e: Exception) {
                _addJetState.value = NetworkResult.Error("Network Error")
            }
        }
    }

    fun deleteJet(email: String, id: String) {
        viewModelScope.launch {
            _deleteJetState.value = NetworkResult.Loading
            try {
                val status = apiService.deleteJet(email, id)
                if (status.status == "success") {
                    _deleteJetState.value = NetworkResult.Success(Unit)
                    fetchJets(email) // refresh list
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

class MainViewModelFactory : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
