package com.startspeler.horeca.screens.crew.tables.qr

import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

actual object QrMatrixGenerator {
    actual fun generate(content: String): Array<BooleanArray>? {
        return try {
            val bitMatrix = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, 256, 256)
            Array(bitMatrix.height) { y ->
                BooleanArray(bitMatrix.width) { x ->
                    bitMatrix.get(x, y)
                }
            }
        } catch (_: Exception) {
            null
        }
    }
}
