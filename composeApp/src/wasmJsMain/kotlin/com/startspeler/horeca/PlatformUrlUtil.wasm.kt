@file:OptIn(kotlin.js.ExperimentalWasmJsInterop::class)

package com.startspeler.horeca

actual fun tableQrBaseUrl(): String = currentOrigin() + "/"

actual fun getTableFromUrl(): String? = currentTableFromSearch()

actual fun isAdminUrl(): Boolean = currentPathname().trimEnd('/') == "/admin"

actual fun saveCustomerName(name: String) {
	saveToLocalStorage("startspeler_name", name)
}

actual fun loadCustomerName(): String? =
	loadFromLocalStorage("startspeler_name")?.takeIf { it.isNotBlank() }

actual fun downloadQrAsPng(content: String, filename: String) = Unit
actual fun printQrCode(content: String, label: String) = Unit

@JsFun("() => window.location.origin")
private external fun currentOrigin(): String

@JsFun("() => new URLSearchParams(window.location.search).get('table')")
private external fun currentTableFromSearch(): String?

@JsFun("() => window.location.pathname")
private external fun currentPathname(): String

@JsFun("(key, value) => window.localStorage.setItem(key, value)")
private external fun saveToLocalStorage(key: String, value: String)

@JsFun("(key) => window.localStorage.getItem(key)")
private external fun loadFromLocalStorage(key: String): String?

