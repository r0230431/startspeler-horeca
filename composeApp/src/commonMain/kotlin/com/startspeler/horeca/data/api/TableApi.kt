package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.tables.CreateTableRequest
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.data.models.tables.UpdateTableRequest
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class TableApi {
    suspend fun getTables(): List<TableResponse> {
        return HttpClientProvider.client.get("/tables").body()
    }

    suspend fun getTableById(id: Int): TableResponse {
        return HttpClientProvider.client.get("/tables/$id").body()
    }

    suspend fun getPublicTableByNumber(tableNumber: Int): TableResponse {
        val response = HttpClientProvider.client.get("/public/tables/by-number/$tableNumber")
        if (!response.status.isSuccess()) {
            throw IllegalStateException(extractApiMessage(response.bodyAsText(), "Tafel niet gevonden. Controleer het tafelnummer."))
        }
        return response.body()
    }

    suspend fun createTable(request: CreateTableRequest): ApiResult<TableResponse> {
        return try {
            val response = HttpClientProvider.client.post("/tables") {
                setBody(request)
            }
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(response.bodyAsText().ifBlank { "Tafel kon niet worden aangemaakt." })
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij aanmaken van tafel.")
        }
    }

    suspend fun updateTable(id: Int, request: UpdateTableRequest): ApiResult<TableResponse> {
        return try {
            val response = HttpClientProvider.client.put("/tables/$id") {
                setBody(request)
            }
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(response.bodyAsText().ifBlank { "Tafel kon niet worden bijgewerkt." })
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van tafel.")
        }
    }

    suspend fun deleteTable(id: Int): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/tables/$id")
            if (response.status.isSuccess()) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(response.bodyAsText().ifBlank { "Tafel kon niet worden verwijderd." })
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van tafel.")
        }
    }
}