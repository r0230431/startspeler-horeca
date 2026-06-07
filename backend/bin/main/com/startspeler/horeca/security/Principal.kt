package com.startspeler.horeca.security

import com.startspeler.horeca.database.enums.CrewRole

data class UserPrincipal(
    val crewMemberId: Int,
    val username: String,
    val role: CrewRole
)

fun UserPrincipal?.isAdmin(): Boolean = this?.role == CrewRole.ADMIN
fun UserPrincipal?.isStaff(): Boolean = this?.role == CrewRole.STAFF