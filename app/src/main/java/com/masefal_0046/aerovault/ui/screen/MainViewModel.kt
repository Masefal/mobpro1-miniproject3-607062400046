package com.masefal_0046.aerovault.ui.screen

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.masefal_0046.aerovault.data.UserPreferencesRepository
import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.model.User
import com.masefal_0046.aerovault.network.AeroVaultApiService
import com.masefal_0046.aerovault.network.NetworkModule
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class NetworkResult<out T> {
    object Idle : NetworkResult<Nothing>()
    object Loading : NetworkResult<Nothing>()
    data class Success<out T>(val data: T) : NetworkResult<T>()
    data class Error(val message: String) : NetworkResult<Nothing>()
}

class MainViewModel(
    private val preferencesRepository: UserPreferencesRepository,
    private val apiService: AeroVaultApiService = NetworkModule.apiService
) : ViewModel() {

    val errorMessage = androidx.compose.runtime.mutableStateOf<String?>(null)
    private val _jetsState = MutableStateFlow<NetworkResult<List<Jet>>>(NetworkResult.Idle)
    val jetsState: StateFlow<NetworkResult<List<Jet>>> = _jetsState.asStateFlow()

    private val _addJetState = MutableStateFlow<NetworkResult<Jet>>(NetworkResult.Idle)
    val addJetState: StateFlow<NetworkResult<Jet>> = _addJetState.asStateFlow()

    private val _deleteJetState = MutableStateFlow<NetworkResult<Unit>>(NetworkResult.Idle)
    val deleteJetState: StateFlow<NetworkResult<Unit>> = _deleteJetState.asStateFlow()

    private val _loginState = MutableStateFlow<NetworkResult<User>>(NetworkResult.Idle)
    val loginState: StateFlow<NetworkResult<User>> = _loginState.asStateFlow()
    
    val userLoginStatus = preferencesRepository.loginStatusFlow

    fun fetchJets() {
        viewModelScope.launch {
            _jetsState.value = NetworkResult.Loading
            try {
                val response = apiService.getJets()
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

    fun addJet(name: String, origin: String, role: String, imageUrl: String) {
        viewModelScope.launch {
            _addJetState.value = NetworkResult.Loading
            try {
                val newJet = Jet(id = "", nama = name, asalNegara = origin, role = role, imageUrl = imageUrl)
                val response = apiService.addJet(newJet)
                if (response.isSuccessful && response.body() != null) {
                    _addJetState.value = NetworkResult.Success(response.body()!!)
                    fetchJets() // refresh list
                } else {
                    _addJetState.value = NetworkResult.Error("Failed to add jet")
                }
            } catch (e: Exception) {
                _addJetState.value = NetworkResult.Error("Network Error")
            }
        }
    }

    fun deleteJet(id: String) {
        viewModelScope.launch {
            _deleteJetState.value = NetworkResult.Loading
            try {
                val response = apiService.deleteJet(id)
                if (response.isSuccessful) {
                    _deleteJetState.value = NetworkResult.Success(Unit)
                    fetchJets() // refresh list
                } else {
                    _deleteJetState.value = NetworkResult.Error("Failed to delete jet")
                }
            } catch (e: Exception) {
                _deleteJetState.value = NetworkResult.Error("Network Error")
            }
        }
    }

    fun login(email: String) {
        viewModelScope.launch {
            _loginState.value = NetworkResult.Loading
            try {
                val request = mapOf("email" to email)
                val response = apiService.login(request)
                if (response.isSuccessful && response.body() != null) {
                    _loginState.value = NetworkResult.Success(response.body()!!)
                    preferencesRepository.saveLoginStatus(true)
                } else {
                    _loginState.value = NetworkResult.Error("Login failed")
                }
            } catch (e: Exception) {
                _loginState.value = NetworkResult.Error("Network Error")
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            preferencesRepository.saveLoginStatus(false)
            _loginState.value = NetworkResult.Idle
        }
    }

    fun resetAddJetState() {
        _addJetState.value = NetworkResult.Idle
    }
    
    fun resetDeleteJetState() {
        _deleteJetState.value = NetworkResult.Idle
    }
}

class MainViewModelFactory(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return MainViewModel(preferencesRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
