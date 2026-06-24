package com.masefal_0046.aerovault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface

import androidx.compose.ui.Modifier
import com.masefal_0046.aerovault.data.UserPreferencesRepository
import com.masefal_0046.aerovault.ui.screen.MainScreen
import com.masefal_0046.aerovault.ui.screen.MainViewModelFactory
import com.masefal_0046.aerovault.ui.theme.AeroVaultTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            AeroVaultTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val preferencesRepository = UserPreferencesRepository(applicationContext)
                    val factory = MainViewModelFactory(preferencesRepository)
                    MainScreen()
                }
            }
        }
    }
}