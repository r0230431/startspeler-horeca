package com.startspeler.horeca.network

import com.startspeler.horeca.auth.TokenStore
import com.startspeler.horeca.core.network.PlatformConfig
import io.ktor.client.HttpClient
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientProvider{
    val client: HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(
                Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    explicitNulls = false
                }
            )
        }

        install(DefaultRequest) {
            url(PlatformConfig.baseUrl)
            header(HttpHeaders.ContentType, ContentType.Application.Json)
            //header(HttpHeaders.Accept, ContentType.Application.Json)
            header(HttpHeaders.Accept, "application/json")

            TokenStore.token?.let { token ->
                header(HttpHeaders.Authorization, "Bearer $token")
            }
        }
    }
}