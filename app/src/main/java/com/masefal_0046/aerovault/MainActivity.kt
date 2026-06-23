package com.masefal_0046.aerovault

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.masefal_0046.aerovault.data.UserPreferencesRepository
import com.masefal_0046.aerovault.ui.screen.LoginScreen
import com.masefal_0046.aerovault.ui.screen.MainScreen
import com.masefal_0046.aerovault.ui.screen.MainViewModel
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
                    val viewModel: MainViewModel = viewModel(factory = factory)
                    
                    AeroVaultApp(viewModel)
                }
            }
        }
    }
}

@Composable
fun AeroVaultApp(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val isUserLoggedIn by viewModel.userLoginStatus.collectAsState(initial = false)

    // Using basic navigation logic based on state
    // For a more robust app, startDestination could be dynamically calculated, but here we use a conditional route
    val startDest = if (isUserLoggedIn) "main" else "login"

    NavHost(navController = navController, startDestination = startDest) {
        composable("login") {
            LoginScreen(
                viewModel = viewModel,
                onLoginSuccess = {
                    navController.navigate("main") {
                        popUpTo("login") { inclusive = true }
                    }
                }
            )
        }
        composable("main") {
            MainScreen(
                viewModel = viewModel,
                onLogout = {
                    navController.navigate("login") {
                        popUpTo("main") { inclusive = true }
                    }
                }
            )
        }
    }
}