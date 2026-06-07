package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.auth.MeResponse
import com.startspeler.horeca.dto.auth.LoginRequest
import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.security.UserPrincipal
import com.startspeler.horeca.service.AuthService
import io.ktor.http.*
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.authRoutes(authService: AuthService) {
    route("/auth") {
        post("/login") {
            val request = call.receive<LoginRequest>()

            val response = authService.login(request)

            if (response == null) {
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse("Ongeldige gebruikersnaam of wachtwoord.")
                )
                return@post
            }

            call.respond(HttpStatusCode.OK, response)
        }

        authenticate("auth-jwt") {
            get("/me") {
                val principal = call.principal<UserPrincipal>()
                    ?: return@get call.respond(HttpStatusCode.Unauthorized)

                call.respond(
                    MeResponse(
                        crewMemberId = principal.crewMemberId,
                        username = principal.username,
                        role = principal.role.name
                    )
                )
            }
        }
    }
}