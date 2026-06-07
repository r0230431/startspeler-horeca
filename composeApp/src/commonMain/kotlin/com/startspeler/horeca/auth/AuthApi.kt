package com.startspeler.horeca.auth

import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess

class AuthApi {
    suspend fun login(username: String, password: String): LoginResponse {
        val response: HttpResponse = HttpClientProvider.client.post("/auth/login") {
            contentType(ContentType.Application.Json)
            setBody(
                LoginRequest(
                    username = username.trim(),
                    password = password
                )
            )
        }

        if (!response.status.isSuccess()) {
            throw Exception("Login mislukt.")
        }

        return response.body()
    }
}