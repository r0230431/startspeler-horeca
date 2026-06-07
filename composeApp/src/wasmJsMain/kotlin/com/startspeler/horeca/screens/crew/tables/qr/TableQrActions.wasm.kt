package com.startspeler.horeca.screens.crew.tables.qr

import androidx.compose.runtime.Composable

@Composable
actual fun rememberTableQrActions(): TableQrActions {
    return TableQrActions(
        canShare = false,
        canPrint = false,
        shareLabel = "Delen niet beschikbaar",
        printLabel = "Printen niet beschikbaar",
        shareQr = { _, _ -> "QR delen is op dit platform nog niet voorzien." },
        printQr = { _, _ -> "QR printen is op dit platform nog niet voorzien." }
    )
}