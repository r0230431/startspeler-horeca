package com.startspeler.horeca.util

import com.startspeler.horeca.core.network.PlatformConfig

fun resolveApiUrl(path: String?): String? {
    val value = path?.trim().orEmpty()
    if (value.isBlank()) return null
    if (value.startsWith("http://") || value.startsWith("https://")) return value

    val base = PlatformConfig.baseUrl.trimEnd('/')
    val normalizedPath = if (value.startsWith("/")) value else "/$value"
    return "$base$normalizedPath"
}
