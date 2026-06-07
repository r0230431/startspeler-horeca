package com.startspeler.horeca.util

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember

@Composable
actual fun rememberImagePicker(
    onImagePicked: (PickedImageFile) -> Unit,
    onError: (String) -> Unit,
): PlatformImagePickerLauncher {
    return remember {
        PlatformImagePickerLauncher {
            onError("Afbeelding uploaden is op dit platform nog niet beschikbaar.")
        }
    }
}
