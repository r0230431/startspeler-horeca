package com.startspeler.horeca.screens.crew.tables.qr

import android.content.Intent
import android.graphics.Bitmap
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.FileProvider
import java.io.File
import java.io.FileOutputStream

@Composable
actual fun rememberTableQrActions(): TableQrActions {
    val context = LocalContext.current

    return TableQrActions(
        canShare = true,
        canPrint = false,
        shareLabel = "Deel QR",
        printLabel = "Print niet beschikbaar",
        shareQr = { content, fileName ->
            try {
                val matrix = QrMatrixGenerator.generate(content)
                    ?: return@TableQrActions "QR-code kon niet worden gegenereerd."

                val size = matrix.size
                val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

                for (y in matrix.indices) {
                    for (x in matrix[y].indices) {
                        bitmap.setPixel(
                            x,
                            y,
                            if (matrix[y][x]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
                        )
                    }
                }

                val file = File(context.cacheDir, "$fileName.png")
                FileOutputStream(file).use { out ->
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                }

                val uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    file
                )

                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "image/png"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    putExtra(Intent.EXTRA_TEXT, content)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }

                context.startActivity(Intent.createChooser(intent, "Deel QR-code"))
                null
            } catch (e: Exception) {
                e.message ?: "QR delen is mislukt."
            }
        },
        printQr = { _, _ ->
            "Printen is in deze versie enkel voorzien op desktop. Op Android kan je de QR wel delen."
        }
    )
}