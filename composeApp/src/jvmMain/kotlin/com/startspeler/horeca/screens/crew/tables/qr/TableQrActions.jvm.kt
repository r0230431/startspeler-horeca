package com.startspeler.horeca.screens.crew.tables.qr

import androidx.compose.runtime.Composable
import java.awt.Graphics
import java.awt.Image
import java.awt.Toolkit
import java.awt.datatransfer.StringSelection
import java.awt.image.BufferedImage
import java.awt.print.PageFormat
import java.awt.print.Printable
import java.awt.print.PrinterJob

@Composable
actual fun rememberTableQrActions(): TableQrActions {
    return TableQrActions(
        canShare = true,
        canPrint = true,
        shareLabel = "Kopieer QR-link",
        printLabel = "Print QR",
        shareQr = { content, _ ->
            try {
                val selection = StringSelection(content)
                Toolkit.getDefaultToolkit().systemClipboard.setContents(selection, null)
                "QR-link gekopieerd naar het klembord."
            } catch (e: Exception) {
                e.message ?: "Kopiëren naar klembord is mislukt."
            }
        },
        printQr = { content, fileName ->
            try {
                val matrix = QrMatrixGenerator.generate(content)
                    ?: return@TableQrActions "QR-code kon niet worden gegenereerd."

                val image = matrixToBufferedImage(matrix)

                val printerJob = PrinterJob.getPrinterJob()
                printerJob.setPrintable(object : Printable {
                    override fun print(graphics: Graphics, pageFormat: PageFormat, pageIndex: Int): Int {
                        if (pageIndex > 0) return Printable.NO_SUCH_PAGE

                        val g = graphics as java.awt.Graphics2D

                        val printableWidth = pageFormat.imageableWidth.toInt()
                        val printableHeight = pageFormat.imageableHeight.toInt()
                        val x = pageFormat.imageableX.toInt()
                        val y = pageFormat.imageableY.toInt()

                        // Haal tafelnummer uit bestandsnaam of content
                        val tableLabel = fileName
                            .substringAfter("tafel_", "")
                            .substringBefore("_qr")
                            .takeIf { it.isNotBlank() }
                            ?.let { "Tafel $it" }
                            ?: "Tafel"

                        // QR kleiner maken: ongeveer de helft van de breedte
                        val qrSize = (printableWidth * 0.25).toInt()

                        // Tekstinstellingen
                        val font = java.awt.Font("SansSerif", java.awt.Font.BOLD, 18)
                        g.font = font
                        val fm = g.fontMetrics
                        val textHeight = fm.height
                        val spacing = 20

                        // Centreer links bovenaan met wat marge
                        val marginLeft = 40
                        val marginTop = 40

                        val startX = x + marginLeft
                        val startY = y + marginTop

                        // QR tekenen
                        g.drawImage(
                            image.getScaledInstance(qrSize, qrSize, Image.SCALE_SMOOTH),
                            startX,
                            startY,
                            null
                        )

                        // Tafelnummer centreren onder QR
                        val textWidth = fm.stringWidth(tableLabel)
                        val textX = startX + (qrSize - textWidth) / 2
                        val textY = startY + qrSize + spacing + fm.ascent
                        g.drawString(tableLabel, textX, textY)

                        return Printable.PAGE_EXISTS
                    }
                })

                if (printerJob.printDialog()) {
                    printerJob.print()
                    null
                } else {
                    "Printen geannuleerd."
                }
            } catch (e: Exception) {
                e.message ?: "Printen van QR-code is mislukt."
            }
        }
    )
}

private fun matrixToBufferedImage(matrix: Array<BooleanArray>): BufferedImage {
    val size = matrix.size
    val image = BufferedImage(size, size, BufferedImage.TYPE_INT_RGB)

    for (y in matrix.indices) {
        for (x in matrix[y].indices) {
            image.setRGB(
                x,
                y,
                if (matrix[y][x]) java.awt.Color.BLACK.rgb else java.awt.Color.WHITE.rgb
            )
        }
    }

    return image
}