package com.startspeler.horeca.data.models.crew

import com.startspeler.horeca.auth.CrewRole
import kotlinx.serialization.Serializable

@Serializable
data class CrewMemberResponse(
    val id: Int,
    val username: String,
    val role: CrewRole,
)
