package com.startspeler.horeca.dto.common

import kotlinx.serialization.Serializable

@Serializable
data class MessageResponse(
    val message: String
)