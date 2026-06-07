package com.startspeler.horeca

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.ComposeViewport
import com.startspeler.horeca.app.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    val initialTableNumber = getTableFromUrl()
    ComposeViewport {
        App(initialQrTableNumber = initialTableNumber)
    }
}