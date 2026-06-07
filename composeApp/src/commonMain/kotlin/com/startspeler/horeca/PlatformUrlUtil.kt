package com.startspeler.horeca

/** Base URL to use in QR codes (e.g. "http://localhost:8081/"). Empty on non-web platforms. */
expect fun tableQrBaseUrl(): String

/** Reads the ?table= query parameter from the current URL. Null on non-web platforms. */
expect fun getTableFromUrl(): String?

/** Returns true when the current URL path is /admin (crew login URL). Always false on non-web platforms. */
expect fun isAdminUrl(): Boolean

/** Saves the customer name persistently (localStorage on web, no-op elsewhere). */
expect fun saveCustomerName(name: String)

/** Loads the previously saved customer name. Null if never saved or on non-web platforms. */
expect fun loadCustomerName(): String?

/** Downloads the QR code for the given content as a PNG file. No-op on non-web platforms. */
expect fun downloadQrAsPng(content: String, filename: String)

/** Opens a print dialog with just the QR code. No-op on non-web platforms. */
expect fun printQrCode(content: String, label: String)
