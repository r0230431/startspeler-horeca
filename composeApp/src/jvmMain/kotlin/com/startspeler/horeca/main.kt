package com.startspeler.horeca

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import com.startspeler.horeca.app.App

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "horeca",
    ) {
        App()
    }
}