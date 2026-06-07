package com.startspeler.horeca.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import java.io.File
import javax.swing.JFileChooser
import javax.swing.filechooser.FileNameExtensionFilter

@Composable
actual fun rememberImagePicker(
    onImagePicked: (PickedImageFile) -> Unit,
    onError: (String) -> Unit,
): PlatformImagePickerLauncher {
    return remember {
        PlatformImagePickerLauncher {
            runCatching {
                val chooser = JFileChooser().apply {
                    dialogTitle = "Kies een productafbeelding"
                    fileFilter = FileNameExtensionFilter(
                        "Afbeeldingen (JPG, JPEG, PNG, WEBP)",
                        "jpg",
                        "jpeg",
                        "png",
                        "webp",
                    )
                    isAcceptAllFileFilterUsed = false
                }

                val result = chooser.showOpenDialog(null)
                if (result != JFileChooser.APPROVE_OPTION) {
                    return@runCatching null
                }

                val file = chooser.selectedFile ?: return@runCatching null
                val mimeType = file.toMimeType()
                PickedImageFile(
                    name = file.name,
                    mimeType = mimeType,
                    bytes = file.readBytes(),
                )
            }.onSuccess { pickedFile ->
                if (pickedFile != null) {
                    onImagePicked(pickedFile)
                }
            }.onFailure { throwable ->
                onError(throwable.message ?: "Afbeelding kon niet geselecteerd worden.")
            }
        }
    }
}

private fun File.toMimeType(): String {
    return when (extension.lowercase()) {
        "jpg", "jpeg" -> "image/jpeg"
        "png" -> "image/png"
        "webp" -> "image/webp"
        else -> "application/octet-stream"
    }
}
