package com.startspeler.horeca.auth

import com.startspeler.horeca.app.AppMode

data class UserSession(
    val appMode: AppMode,
    val crewRole: CrewRole,
    val crewMemberId: Int? = null,
    val username: String? = null,
    val token: String? = null
)