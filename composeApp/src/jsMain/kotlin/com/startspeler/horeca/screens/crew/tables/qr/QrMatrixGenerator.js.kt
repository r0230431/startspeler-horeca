package com.startspeler.horeca.screens.crew.tables.qr

@JsModule("qrcode")
@JsNonModule
external object QRCodeLib {
    fun create(text: String, options: dynamic = definedExternally): dynamic
}

actual object QrMatrixGenerator {
    actual fun generate(content: String): Array<BooleanArray>? {
        return try {
            val qr = QRCodeLib.create(content)
            val size = qr.modules.size.unsafeCast<Int>()
            val data = qr.modules.data
            Array(size) { y ->
                BooleanArray(size) { x ->
                    data[y * size + x].unsafeCast<Int>() != 0
                }
            }
        } catch (_: Throwable) {
            null
        }
    }
}
