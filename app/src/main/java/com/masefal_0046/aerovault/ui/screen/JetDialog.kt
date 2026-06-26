package com.masefal_0046.aerovault.ui.screen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.canhub.cropper.CropImageView
import java.io.ByteArrayOutputStream

private fun getCroppedImage(
    resolver: ContentResolver,
    result: CropImageView.CropResult
): Bitmap? {
    if (!result.isSuccessful) {
        Log.e("IMAGE", "Error: ${result.error}")
        return null
    }

    val uri = result.uriContent ?: return null

    return if (Build.VERSION.SDK_INT < Build.VERSION_CODES.P) {
        @Suppress("DEPRECATION")
        MediaStore.Images.Media.getBitmap(resolver, uri)
    } else {
        val source = ImageDecoder.createSource(resolver, uri)
        ImageDecoder.decodeBitmap(source)
    }
}

@Composable
fun JetDialog(
    viewModel: MainViewModel,
    userEmail: String, // Ingat, ini bertindak sebagai token lo ya
    onDismiss: () -> Unit
) {
    val addJetState by viewModel.addJetState.collectAsState()
    val context = LocalContext.current

    var name by remember { mutableStateOf("") }
    var origin by remember { mutableStateOf("") }
    var role by remember { mutableStateOf("") }

    var bitmap by remember { mutableStateOf<Bitmap?>(null) }
    var errorText by remember { mutableStateOf("") }

    val launcher = rememberLauncherForActivityResult(CropImageContract()) { result ->
        val croppedBitmap = getCroppedImage(context.contentResolver, result)
        if (croppedBitmap != null) {
            bitmap = croppedBitmap
        }
    }

    LaunchedEffect(addJetState) {
        if (addJetState is NetworkResult.Success) {
            viewModel.resetAddJetState()
            onDismiss()
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Tambah Jet Tempur") },
        text = {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Preview Jet",
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(150.dp)
                            .padding(bottom = 8.dp)
                    )
                }

                Button(onClick = {
                    val options = CropImageContractOptions(
                        null, CropImageOptions(
                            imageSourceIncludeGallery = true,
                            imageSourceIncludeCamera = false,
                            fixAspectRatio = true
                        )
                    )
                    launcher.launch(options)
                }) {
                    Text(if (bitmap == null) "Pilih Foto Jet" else "Ganti Foto")
                }

                Spacer(modifier = Modifier.height(16.dp))

                // --- BAGIAN TEKS ---
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Jet") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Asal Negara") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (Peran)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
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
                        if (name.isBlank() || origin.isBlank() || role.isBlank() || bitmap == null) {
                            errorText = "Semua kolom dan gambar wajib diisi!"
                        } else {
                            errorText = ""

                            val stream = ByteArrayOutputStream()
                            bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                            val imageBytes = stream.toByteArray()

                            viewModel.addJet("Bearer $userEmail", name, origin, role, imageBytes)
                        }
                    }
                ) {
                    Text("Simpan")
                }
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
