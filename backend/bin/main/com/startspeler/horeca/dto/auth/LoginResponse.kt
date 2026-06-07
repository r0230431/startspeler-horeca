package com.startspeler.horeca.dto.auth

import kotlinx.serialization.Serializable

@Serializable
data class LoginResponse(
    val token: String,
    val crewMemberId: Int,
    val username: String,
    val role: String
)