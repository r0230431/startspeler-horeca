package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.customers.CreateCustomerRequest
import com.startspeler.horeca.data.models.customers.CustomerResponse
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode

class CustomerApi {
    suspend fun getCustomers(): List<CustomerResponse> {
        return HttpClientProvider.client.get("/customers").body()
    }

    suspend fun searchCustomers(query: String): List<CustomerResponse> {
        if (query.isBlank()) return emptyList()
        return HttpClientProvider.client.get("/customers/search?query=$query").body()
    }

    suspend fun createCustomer(username: String): CustomerResponse {
        val response = HttpClientProvider.client.post("/customers") {
            setBody(CreateCustomerRequest(username = username.trim()))
        }

        if (response.status != HttpStatusCode.Created && response.status != HttpStatusCode.OK) {
            throw IllegalStateException(extractApiMessage(response.bodyAsText(), "Klant kon niet worden aangemaakt."))
        }

        return response.body()
    }

    suspend fun deleteCustomer(id: Int): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/customers/$id")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> ApiResult.Success(Unit)
                else -> ApiResult.Error(response.bodyAsText().ifBlank { "Klant kon niet worden verwijderd." })
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van klant.")
        }
    }

    suspend fun deleteAllCustomers(): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/customers")
            when (response.status) {
                HttpStatusCode.OK, HttpStatusCode.NoContent -> ApiResult.Success(Unit)
                else -> ApiResult.Error(response.bodyAsText().ifBlank { "Klanten konden niet worden verwijderd." })
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van alle klanten.")
        }
    }
}