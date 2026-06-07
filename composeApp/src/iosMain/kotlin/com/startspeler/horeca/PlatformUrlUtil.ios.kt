package com.startspeler.horeca

actual fun tableQrBaseUrl(): String = ""
actual fun getTableFromUrl(): String? = null
actual fun isAdminUrl(): Boolean = false
actual fun saveCustomerName(name: String) = Unit
actual fun loadCustomerName(): String? = null
actual fun downloadQrAsPng(content: String, filename: String) = Unit
actual fun printQrCode(content: String, label: String) = Unit
