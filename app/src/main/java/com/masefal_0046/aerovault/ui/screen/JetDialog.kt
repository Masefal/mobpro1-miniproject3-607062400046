package com.masefal_0046.aerovault.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun JetDialog(
    viewModel: MainViewModel,
    onDismiss: () -> Unit
) {
    val addJetState by viewModel.addJetState.collectAsState()

    var name by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }
    var imageUrl by remember { mutableStateOf("") }
    var errorText by remember { mutableStateOf("") }

    LaunchedEffect(addJetState) {
        if (addJetState is NetworkResult.Success) {
            viewModel.resetAddJetState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Jet") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Name") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Origin Country") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role") },
                    singleLine = true
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = imageUrl,
                    onValueChange = { imageUrl = it },
                    label = { Text("Image URL") },
                    singleLine = true
                )
                
                if (errorText.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(text = errorText, color = MaterialTheme.colorScheme.error)
                }
                
                if (addJetState is NetworkResult.Error) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = (addJetState as NetworkResult.Error).message,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            if (addJetState is NetworkResult.Loading) {
                CircularProgressIndicator()
            } else {
                TextButton(
                    onClick = {
                        if (name.isBlank() || origin.isBlank() || role.isBlank() || imageUrl.isBlank()) {
                            errorText = "All fields must be filled"
                        } else {
                            errorText = ""
                            viewModel.addJet(name, origin, role, imageUrl)
                        }
                    }
                ) {
                    Text("Save")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
