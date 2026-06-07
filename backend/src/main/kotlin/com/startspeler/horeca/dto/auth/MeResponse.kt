package com.startspeler.horeca.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class MeResponse(
    val crewMemberId: Int,
    val username: String,
    val role: String
)