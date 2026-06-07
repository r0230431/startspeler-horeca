package com.startspeler.horeca

import com.startspeler.horeca.screens.crew.tables.qr.QrMatrixGenerator
import kotlinx.browser.document
import kotlinx.browser.window
import org.w3c.dom.HTMLAnchorElement
import org.w3c.dom.HTMLCanvasElement

actual fun tableQrBaseUrl(): String =
    js("window.location.origin").unsafeCast<String>() + "/"

actual fun getTableFromUrl(): String? =
    js("new URLSearchParams(window.location.search).get('table')").unsafeCast<String?>()

actual fun isAdminUrl(): Boolean =
    js("window.location.pathname").unsafeCast<String>().trimEnd('/') == "/admin"

actual fun saveCustomerName(name: String) {
    window.localStorage.setItem("startspeler_name", name)
}

actual fun loadCustomerName(): String? =
    window.localStorage.getItem("startspeler_name")?.takeIf { it.isNotBlank() }

private fun buildQrCanvas(content: String, cellSize: Int = 10, padding: Int = 20): HTMLCanvasElement? {
    val matrix = QrMatrixGenerator.generate(content) ?: return null
    val size = matrix.size
    val canvasSize = size * cellSize + padding * 2

    val canvas = document.createElement("canvas") as HTMLCanvasElement
    canvas.width = canvasSize
    canvas.height = canvasSize

    val ctx = canvas.getContext("2d").asDynamic()
    ctx.fillStyle = "white"
    ctx.fillRect(0, 0, canvasSize, canvasSize)
    ctx.fillStyle = "black"
    for (y in 0 until size) {
        for (x in 0 until size) {
            if (matrix[y][x]) {
                ctx.fillRect(padding + x * cellSize, padding + y * cellSize, cellSize, cellSize)
            }
        }
    }
    return canvas
}

actual fun downloadQrAsPng(content: String, filename: String) {
    val canvas = buildQrCanvas(content) ?: return
    val dataUrl = canvas.toDataURL("image/png")
    val link = document.createElement("a") as HTMLAnchorElement
    link.download = filename
    link.href = dataUrl
    document.body?.appendChild(link)
    link.click()
    document.body?.removeChild(link)
}

actual fun printQrCode(content: String, label: String) {
    val canvas = buildQrCanvas(content, cellSize = 12, padding = 30) ?: return
    val dataUrl = canvas.toDataURL("image/png")
    val printWindow = window.open("", "_blank") ?: return
    val html = """
        <!DOCTYPE html>
        <html>
        <head>
            <title>QR - $label</title>
            <style>
                body { text-align: center; font-family: sans-serif; padding: 20px; }
                img { max-width: 280px; }
                h2 { margin-bottom: 12px; }
                p { word-break: break-all; font-size: 11px; color: #888; margin-top: 10px; }
            </style>
        </head>
        <body>
            <h2>$label</h2>
            <img src="$dataUrl" />
            <p>$content</p>
            <script>window.onload = function() { window.print(); };<\/script>
        </body>
        </html>
    """.trimIndent()
    printWindow.document.asDynamic().write(html)
    printWindow.document.asDynamic().close()
}
