package com.startspeler.horeca.screens.crew.tables.qr

expect object QrMatrixGenerator {
    fun generate(content: String): Array<BooleanArray>?
}
