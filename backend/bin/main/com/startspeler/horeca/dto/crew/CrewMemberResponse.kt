package com.startspeler.horeca.dto.crew

import com.startspeler.horeca.database.enums.CrewRole
import kotlinx.serialization.Serializable

@Serializable
data class CrewMemberResponse(
    val id: Int,
    val username: String,
    val role: CrewRole
)