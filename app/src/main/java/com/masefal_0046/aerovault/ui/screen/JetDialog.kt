package com.masefal_0046.aerovault.ui.screen

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.window.Dialog
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
    userEmail: String,
    onDismiss: () -> Unit
) {
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

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier.padding(16.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Tambah Jet Tempur", style = MaterialTheme.typography.titleLarge)

                if (bitmap != null) {
                    Image(
                        bitmap = bitmap!!.asImageBitmap(),
                        contentDescription = "Preview Jet",
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(top = 12.dp)
                    )
                } else {
                    OutlinedButton(
                        onClick = {
                            val options = CropImageContractOptions(
                                null, CropImageOptions(
                                    imageSourceIncludeGallery = true,
                                    imageSourceIncludeCamera = false,
                                    fixAspectRatio = true
                                )
                            )
                            launcher.launch(options)
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(1f)
                            .padding(top = 12.dp)
                    ) {
                        Text("Pilih Foto Jet")
                    }
                }

                if (bitmap != null) {
                    OutlinedButton(
                        onClick = {
                            val options = CropImageContractOptions(
                                null, CropImageOptions(
                                    imageSourceIncludeGallery = true,
                                    imageSourceIncludeCamera = false,
                                    fixAspectRatio = true
                                )
                            )
                            launcher.launch(options)
                        },
                        modifier = Modifier.padding(top = 8.dp)
                    ) {
                        Text("Ganti Foto")
                    }
                }

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Jet") },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = origin,
                    onValueChange = { origin = it },
                    label = { Text("Asal Negara") },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )
                OutlinedTextField(
                    value = role,
                    onValueChange = { role = it },
                    label = { Text("Role (Peran)") },
                    maxLines = 1,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        imeAction = ImeAction.Done
                    ),
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
                )

                if (errorText.isNotEmpty()) {
                    Text(
                        text = errorText,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Row(
                    modifier = Modifier.fillMaxWidth().padding(top = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    OutlinedButton(
                        onClick = { onDismiss() },
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Batal")
                    }
                    OutlinedButton(
                        onClick = {
                            if (name.isBlank() || origin.isBlank() || role.isBlank() || bitmap == null) {
                                errorText = "Semua kolom dan gambar wajib diisi!"
                            } else {
                                errorText = ""

                                val stream = ByteArrayOutputStream()
                                bitmap!!.compress(Bitmap.CompressFormat.JPEG, 80, stream)
                                val imageBytes = stream.toByteArray()

                                viewModel.saveData(userEmail, name, origin, role, imageBytes)
                                onDismiss()
                            }
                        },
                        enabled = name.isNotBlank() &&
                                origin.isNotBlank() &&
                                role.isNotBlank() &&
                                bitmap != null,
                        modifier = Modifier.padding(8.dp)
                    ) {
                        Text("Simpan")
                    }
                }
            }
        }
    }
}
