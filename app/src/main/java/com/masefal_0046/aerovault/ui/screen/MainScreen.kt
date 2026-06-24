package com.masefal_0046.aerovault.ui.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.ClearCredentialException
import androidx.credentials.exceptions.GetCredentialException
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.masefal_0046.aerovault.BuildConfig
import com.masefal_0046.aerovault.R
import com.masefal_0046.aerovault.network.UserDataStore
import com.masefal_0046.aerovault.model.Jet
import com.masefal_0046.aerovault.model.User
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import android.util.Log
import android.content.Context

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen() {
    val context = LocalContext.current
    
    val viewModel: MainViewModel = viewModel(factory = MainViewModelFactory())
    
    val dataStore = UserDataStore(context)
    val user by dataStore.userFlow.collectAsState(User())

    var showProfileDialog by remember { mutableStateOf(false) }
    var showJetDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(text = stringResource(id = R.string.app_name))
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = {
                        if (user.email.isEmpty()) {
                            CoroutineScope(Dispatchers.IO).launch { signIn(context, dataStore) }
                        } else {
                            showProfileDialog = true
                        }
                    }) {
                        Icon(
                            imageVector = Icons.Default.AccountCircle,
                            contentDescription = "Profil",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                showJetDialog = true
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Jet"
                )
            }
        }
    ) { innerPadding ->
        ScreenContent(viewModel, user.email, Modifier.padding(innerPadding))

        if (showProfileDialog) {
            ProfileDialog(
                user = user,
                onDismissRequest = { showProfileDialog = false },
                onConfirmation = { 
                    showProfileDialog = false 
                    CoroutineScope(Dispatchers.IO).launch { signOut(context, dataStore) }
                }
            )
        }

        if (showJetDialog) {
            JetDialog(
                viewModel = viewModel,
                userEmail = user.email,
                onDismiss = { showJetDialog = false }
            )
        }
    }
}

@Composable
fun ScreenContent(viewModel: MainViewModel, userEmail: String, modifier: Modifier = Modifier) {
    val jetsState by viewModel.jetsState.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }
    var jetToDelete by remember { mutableStateOf<Jet?>(null) }

    LaunchedEffect(userEmail) {
        if (userEmail.isNotEmpty()) {
            viewModel.fetchJets(userEmail)
        }
    }

    when (jetsState) {
        is NetworkResult.Idle, is NetworkResult.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }

        is NetworkResult.Success -> {
            val data = (jetsState as NetworkResult.Success).data
            LazyVerticalGrid(
                modifier = modifier
                    .fillMaxSize()
                    .padding(4.dp),
                columns = GridCells.Fixed(2),
                contentPadding = PaddingValues(bottom = 80.dp)
            ) {
                items(data) { jet ->
                    JetItem(
                        jet = jet,
                        onDeleteClick = { 
                            jetToDelete = jet
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }

        is NetworkResult.Error -> {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = stringResource(id = R.string.error))
                Button(
                    onClick = { if (userEmail.isNotEmpty()) viewModel.fetchJets(userEmail) },
                    modifier = Modifier.padding(top = 16.dp),
                    contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                ) {
                    Text(text = stringResource(id = R.string.try_again))
                }
            }
        }
    }
    
    if (showDeleteDialog && jetToDelete != null) {
        AlertDialog(
            onDismissRequest = {
                showDeleteDialog = false
                jetToDelete = null
            },
            title = { Text("Hapus Jet") },
            text = { Text("Apakah Anda yakin ingin menghapus ${jetToDelete?.nama}?") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deleteJet(userEmail, jetToDelete!!.id)
                    showDeleteDialog = false
                    jetToDelete = null
                }) {
                    Text("Hapus")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    jetToDelete = null
                }) {
                    Text("Batal")
                }
            }
        )
    }
}

@Composable
fun JetItem(jet: Jet, onDeleteClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(4.dp)
            .border(1.dp, Color.Gray),
        contentAlignment = Alignment.BottomCenter
    ) {
        AsyncImage(
            model = ImageRequest.Builder(LocalContext.current)
                .data(jet.imageUrl)
                .crossfade(true)
                .build(),
            contentDescription = "Gambar ${jet.nama}",
            placeholder = androidx.compose.ui.res.painterResource(id = R.drawable.loading_img),
            error = androidx.compose.ui.res.painterResource(id = R.drawable.broken_img),
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            contentScale = ContentScale.Crop
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp)
                .background(Color(red = 0f, green = 0f, blue = 0f, alpha = 0.5f))
                .padding(4.dp)
        ) {
            Text(
                text = jet.nama,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = "${jet.asalNegara} - ${jet.role}",
                fontStyle = FontStyle.Italic,
                fontSize = 14.sp,
                color = Color.White
            )
        }

        IconButton(
            onClick = onDeleteClick,
            modifier = Modifier.align(Alignment.TopEnd)
        ) {
            Icon(
                imageVector = Icons.Default.Delete,
                contentDescription = "Hapus Jet",
                tint = Color.Red
            )
        }
    }
}

private suspend fun signIn(context: Context, dataStore: UserDataStore) {
    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(BuildConfig.API_KEY)
        .build()

    val request: GetCredentialRequest = GetCredentialRequest.Builder()
        .addCredentialOption(googleIdOption)
        .build()

    try {
        val credentialManager = CredentialManager.create(context)
        val result = credentialManager.getCredential(context, request)
        handleSignIn(result, dataStore)
    } catch (e: GetCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, dataStore: UserDataStore) {
    val credential = result.credential
    if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
        try {
            val googleId = GoogleIdTokenCredential.createFrom(credential.data)
            val name = googleId.displayName ?: ""
            val email = googleId.id
            val photoUrl = googleId.profilePictureUri?.toString() ?: ""
            dataStore.saveData(User(name, email, photoUrl))
        } catch (e: GoogleIdTokenParsingException) {
            Log.e("SIGN-IN", "Error: ${e.message}")
        }
    } else {
        Log.e("SIGN-IN", "Error: unrecognized custom credential type.")
    }
}

private suspend fun signOut(context: Context, dataStore: UserDataStore) {
    try {
        val credentialManager = CredentialManager.create(context)
        credentialManager.clearCredentialState(ClearCredentialStateRequest())
        dataStore.saveData(User())
    } catch (e: ClearCredentialException) {
        Log.e("SIGN-IN", "Error: ${e.errorMessage}")
    }
}
