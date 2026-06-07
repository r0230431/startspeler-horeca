package com.startspeler.horeca.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class ErrorResponse(
    val message: String
)