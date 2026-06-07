package com.startspeler.horeca.data.api

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

internal fun extractApiMessage(body: String?, fallback: String): String {
    val raw = body?.trim().orEmpty()
    if (raw.isBlank()) return fallback

    return try {
        Json.parseToJsonElement(raw)
            .jsonObject["message"]
            ?.jsonPrimitive
            ?.content
            ?.takeIf { it.isNotBlank() }
            ?: raw
    } catch (_: Exception) {
        raw
    }
}
