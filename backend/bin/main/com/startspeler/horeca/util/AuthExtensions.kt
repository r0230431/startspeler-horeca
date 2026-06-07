package com.startspeler.horeca.util

import com.startspeler.horeca.security.UserPrincipal
import com.startspeler.horeca.security.isAdmin
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.response.*

suspend fun ApplicationCall.requireAdmin(): Boolean {
    val principal = principal<UserPrincipal>()

    if (principal == null) {
        respond(HttpStatusCode.Unauthorized)
        return false
    }

    if (!principal.isAdmin()) {
        respond(HttpStatusCode.Forbidden, mapOf("message" to "Geen toegang."))
        return false
    }

    return true
}

suspend fun ApplicationCall.requireUser(): UserPrincipal? {
    val principal = principal<UserPrincipal>()

    if (principal == null) {
        respond(HttpStatusCode.Unauthorized)
        return null
    }

    return principal
}