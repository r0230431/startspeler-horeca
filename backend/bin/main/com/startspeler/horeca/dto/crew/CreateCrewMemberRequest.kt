package com.startspeler.horeca.dto.crew

import com.startspeler.horeca.database.enums.CrewRole
import kotlinx.serialization.Serializable

@Serializable
data class CreateCrewMemberRequest(
    val username: String,
    val password: String,
    val role: CrewRole
)