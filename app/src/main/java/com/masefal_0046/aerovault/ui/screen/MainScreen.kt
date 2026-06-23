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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.masefal_0046.aerovault.model.Jet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onLogout: () -> Unit
) {
    val jetsState by viewModel.jetsState.collectAsState()
    
    var showAddDialog by remember { mutableStateOf(false) }
    var showProfileDialog by remember { mutableStateOf(false) }
    var jetToDelete by remember { mutableStateOf<Jet?>(null) }

    LaunchedEffect(Unit) {
        viewModel.fetchJets()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AeroVault") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
                actions = {
                    IconButton(onClick = { showProfileDialog = true }) {
                        Icon(
                            imageVector = Icons.Filled.AccountCircle, 
                            contentDescription = "Profile",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Tambah Jet"
                )
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center
        ) {
            when (val state = jetsState) {
                is NetworkResult.Loading -> {
                    CircularProgressIndicator()
                }
                is NetworkResult.Success -> {
                    LazyVerticalGrid(
                        columns = GridCells.Fixed(2),
                        contentPadding = PaddingValues(bottom = 80.dp),
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(4.dp)
                    ) {
                        items(state.data) { jet ->
                            JetItem(
                                jet = jet,
                                onDeleteClick = { jetToDelete = jet }
                            )
                        }
                    }
                }
                is NetworkResult.Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = state.message,
                            color = MaterialTheme.colorScheme.error
                        )
                        Button(
                            onClick = { viewModel.fetchJets() },
                            modifier = Modifier.padding(top = 16.dp),
                            contentPadding = PaddingValues(horizontal = 32.dp, vertical = 16.dp)
                        ) {
                            Text("Try Again")
                        }
                    }
                }
                else -> {}
            }
        }
    }

    if (showAddDialog) {
        JetDialog(
            viewModel = viewModel,
            onDismiss = { showAddDialog = false }
        )
    }

    if (showProfileDialog) {
        ProfileDialog(
            viewModel = viewModel,
            onDismiss = { showProfileDialog = false },
            onLogout = onLogout
        )
    }

    jetToDelete?.let { jet ->
        AlertDialog(
            onDismissRequest = { jetToDelete = null },
            title = { Text("Hapus Data") },
            text = { Text("Apakah Anda yakin ingin menghapus ${jet.nama}?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteJet(jet.id)
                        jetToDelete = null
                    }
                ) {
                    Text("Hapus", color = Color.Red)
                }
            },
            dismissButton = {
                TextButton(onClick = { jetToDelete = null }) {
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
            model = jet.imageUrl,
            contentDescription = "Gambar ${jet.nama}",
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
