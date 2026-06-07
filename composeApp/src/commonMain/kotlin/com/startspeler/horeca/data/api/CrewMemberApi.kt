package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.crew.CreateCrewMemberRequest
import com.startspeler.horeca.data.models.crew.CrewMemberResponse
import com.startspeler.horeca.data.models.crew.UpdateCrewMemberRequest
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class CrewMemberApi {
    suspend fun getCrewMembers(): List<CrewMemberResponse> {
        return HttpClientProvider.client.get("/crew-members").body()
    }

    suspend fun createCrewMember(request: CreateCrewMemberRequest): ApiResult<CrewMemberResponse> {
        return try {
            val response = HttpClientProvider.client.post("/crew-members") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Medewerker kon niet worden aangemaakt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij aanmaken van medewerker.")
        }
    }

    suspend fun updateCrewMember(id: Int, request: UpdateCrewMemberRequest): ApiResult<CrewMemberResponse> {
        return try {
            val response = HttpClientProvider.client.put("/crew-members/$id") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Medewerker kon niet worden bijgewerkt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van medewerker.")
        }
    }

    suspend fun deleteCrewMember(id: Int): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/crew-members/$id")
            if (response.status.isSuccess()) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Medewerker kon niet worden verwijderd."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van medewerker.")
        }
    }
}
