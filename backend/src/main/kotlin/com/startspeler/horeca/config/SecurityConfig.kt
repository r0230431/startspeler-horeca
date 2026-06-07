package com.startspeler.horeca.config

import com.auth0.jwt.JWT
import com.startspeler.horeca.database.enums.CrewRole
import com.startspeler.horeca.security.JwtConfig
import com.startspeler.horeca.security.UserPrincipal
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*

fun Application.configureSecurity(jwtConfig: JwtConfig) {
    install(Authentication) {
        jwt("auth-jwt") {
            realm = "startspeler"
            verifier(
                JWT
                    .require(jwtConfig.algorithm())
                    .withIssuer(jwtConfig.issuer())
                    .withAudience(jwtConfig.audience())
                    .build()
            )

            validate { credential ->
                val crewMemberId = credential.payload.getClaim("crewMemberId").asInt()
                val username = credential.payload.getClaim("username").asString()
                val roleString = credential.payload.getClaim("role").asString()

                if (crewMemberId != null && username != null && roleString != null) {
                    UserPrincipal(
                        crewMemberId = crewMemberId,
                        username = username,
                        role = CrewRole.valueOf(roleString)
                    )
                } else {
                    null
                }
            }
        }
    }
}