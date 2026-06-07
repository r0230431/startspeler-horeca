package com.startspeler.horeca.util

import android.provider.OpenableColumns
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun rememberImagePicker(
    onImagePicked: (PickedImageFile) -> Unit,
    onError: (String) -> Unit,
): PlatformImagePickerLauncher {
    val context = LocalContext.current

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        if (uri == null) return@rememberLauncherForActivityResult

        runCatching {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri)
                ?: throw IllegalArgumentException("Het gekozen bestand heeft geen geldig afbeeldingstype.")

            val fileName = resolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)
                ?.use { cursor ->
                    val nameColumn = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (cursor.moveToFirst() && nameColumn >= 0) {
                        cursor.getString(nameColumn)
                    } else {
                        null
                    }
                }
                ?: "product-image"

            val bytes = resolver.openInputStream(uri)?.use { it.readBytes() }
                ?: throw IllegalStateException("Afbeelding kon niet gelezen worden.")

            PickedImageFile(
                name = fileName,
                mimeType = mimeType,
                bytes = bytes,
            )
        }.onSuccess(onImagePicked)
            .onFailure { throwable ->
                onError(throwable.message ?: "Afbeelding kon niet geselecteerd worden.")
            }
    }

    return remember(launcher) {
        PlatformImagePickerLauncher {
            launcher.launch("image/*")
        }
    }
}
