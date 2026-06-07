package com.startspeler.horeca

import com.startspeler.horeca.config.DatabaseFactory
import com.startspeler.horeca.config.configureRouting
import com.startspeler.horeca.config.configureSecurity
import com.startspeler.horeca.security.JwtConfig
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.*
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import java.io.File
import java.util.TimeZone

fun main(args: Array<String>) {
    TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    loadDotEnv()

    io.ktor.server.netty.EngineMain.main(args)
}

/**
 * Reads a local `.env` file (if present) and exposes its values as JVM system
 * properties so Ktor's `application.conf` can pick them up via `${?VAR}`.
 * Real environment variables and existing system properties always win,
 * which keeps production behaviour (systemd EnvironmentFile) unchanged.
 */
private fun loadDotEnv() {
    val file = listOf(File("backend/.env"), File(".env")).firstOrNull { it.isFile } ?: return

    file.readLines()
        .map { it.trim() }
        .filter { it.isNotEmpty() && !it.startsWith("#") }
        .forEach { line ->
            val separator = line.indexOf('=')
            if (separator <= 0) return@forEach

            val key = line.substring(0, separator).trim()
            val value = line.substring(separator + 1).trim()
                .removeSurrounding("\"")
                .removeSurrounding("'")

            if (key.isEmpty()) return@forEach
            if (System.getenv(key) != null) return@forEach
            if (!System.getProperty(key).isNullOrBlank()) return@forEach

            System.setProperty(key, value)
        }
}

fun Application.module() {
    DatabaseFactory.init(environment.config)

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