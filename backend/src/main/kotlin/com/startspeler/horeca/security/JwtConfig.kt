package com.startspeler.horeca.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.startspeler.horeca.database.enums.CrewRole
import java.util.Date

class JwtConfig(
    private val secret: String,
    private val issuer: String,
    private val audience: String,
    private val expirationMillis: Long
) {
    private val algorithm = Algorithm.HMAC256(secret)

    fun generateToken(
        crewMemberId: Int,
        username: String,
        role: CrewRole
    ): String {
        return JWT.create()
            .withAudience(audience)
            .withIssuer(issuer)
            .withClaim("crewMemberId", crewMemberId)
            .withClaim("username", username)
            .withClaim("role", role.name)
            .withExpiresAt(Date(System.currentTimeMillis() + expirationMillis))
            .sign(algorithm)
    }

    fun algorithm(): Algorithm = algorithm
    fun issuer(): String = issuer
    fun audience(): String = audience
}