package com.startspeler.horeca.screens.crew.tables

import com.startspeler.horeca.tableQrBaseUrl

fun generateTableQrContent(tableNumber: Int): String {
    val base = tableQrBaseUrl()
    return if (base.isNotBlank()) "${base}?table=$tableNumber"
    else "startspeler://customer?table=$tableNumber"
}
