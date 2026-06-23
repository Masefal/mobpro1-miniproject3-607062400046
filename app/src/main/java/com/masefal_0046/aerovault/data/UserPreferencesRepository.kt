package com.masefal_0046.aerovault.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "user_prefs")

class UserPreferencesRepository(private val context: Context) {
    
    private val LOGIN_STATUS = booleanPreferencesKey("login_status")

    val loginStatusFlow: Flow<Boolean> = context.dataStore.data.map { preferences ->
        preferences[LOGIN_STATUS] ?: false
    }

    suspend fun saveLoginStatus(isLoggedIn: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[LOGIN_STATUS] = isLoggedIn
        }
    }
}
