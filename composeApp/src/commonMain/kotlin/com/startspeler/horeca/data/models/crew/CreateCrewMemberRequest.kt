package com.startspeler.horeca.data.models.crew

import com.startspeler.horeca.auth.CrewRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateCrewMemberRequest(
    val username: String,
    val password: String,
    val role: CrewRole,
)
