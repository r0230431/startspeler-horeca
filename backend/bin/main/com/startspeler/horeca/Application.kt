package com.startspeler.horeca

import com.startspeler.horeca.config.DatabaseFactory
import com.startspeler.horeca.config.configureRouting
import com.startspeler.horeca.config.configureSecurity
import com.startspeler.horeca.security.JwtConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import java.util.TimeZone

fun main(args: Array<String>) {
//    val hash = at.favre.lib.crypto.bcrypt.BCrypt
//        .withDefaults()
//        .hashToString(12, "staff123".toCharArray())
//
//    println("HASH: $hash")
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

    io.ktor.server.netty.EngineMain.main(args)
}

fun Application.module() {
    DatabaseFactory.init()

    install(ContentNegotiation) {
        json()
    }

    val jwtConfig = JwtConfig(
        secret = environment.config.property("jwt.secret").getString(),
        issuer = environment.config.property("jwt.issuer").getString(),
        audience = environment.config.property("jwt.audience").getString(),
        expirationMillis = environment.config.property("jwt.expirationMs").getString().toLong()
    )

    configureSecurity(jwtConfig)
    configureRouting()
}