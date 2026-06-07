package com.startspeler.horeca.screens.crew.tables.qr

import androidx.compose.runtime.Composable

data class TableQrActions(
    val canShare: Boolean,
    val canPrint: Boolean,
    val shareLabel: String,
    val printLabel: String,
    val shareQr: (content: String, fileName: String) -> String?,
    val printQr: (content: String, fileName: String) -> String?,
)

@Composable
expect fun rememberTableQrActions(): TableQrActions