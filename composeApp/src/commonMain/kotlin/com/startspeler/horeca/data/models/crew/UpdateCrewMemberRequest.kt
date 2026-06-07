package com.startspeler.horeca.data.models.crew

import com.startspeler.horeca.auth.CrewRole
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCrewMemberRequest(
    val username: String,
    val role: CrewRole,
    val password: String? = null,
)
