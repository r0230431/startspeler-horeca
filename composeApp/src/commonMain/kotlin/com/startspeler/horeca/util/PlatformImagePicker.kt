package com.startspeler.horeca.util

import androidx.compose.runtime.Composable

data class PickedImageFile(
    val name: String,
    val mimeType: String,
    val bytes: ByteArray,
)

class PlatformImagePickerLauncher(
    private val onLaunch: () -> Unit,
) {
    fun launch() = onLaunch()
}

@Composable
expect fun rememberImagePicker(
    onImagePicked: (PickedImageFile) -> Unit,
    onError: (String) -> Unit,
): PlatformImagePickerLauncher
