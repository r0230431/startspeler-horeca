package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.discounts.CreateDiscountRequest
import com.startspeler.horeca.data.models.discounts.DiscountResponse
import com.startspeler.horeca.data.models.discounts.UpdateDiscountRequest
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class DiscountApi {
    suspend fun getDiscounts(): List<DiscountResponse> {
        return HttpClientProvider.client.get("/discounts").body()
    }

    suspend fun createDiscount(request: CreateDiscountRequest): ApiResult<DiscountResponse> {
        return try {
            val response = HttpClientProvider.client.post("/discounts") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Korting kon niet worden aangemaakt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij aanmaken van korting.")
        }
    }

    suspend fun updateDiscount(id: Int, request: UpdateDiscountRequest): ApiResult<DiscountResponse> {
        return try {
            val response = HttpClientProvider.client.put("/discounts/$id") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Korting kon niet worden bijgewerkt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van korting.")
        }
    }

    suspend fun deleteDiscount(id: Int): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/discounts/$id")
            if (response.status.isSuccess()) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Korting kon niet worden verwijderd."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van korting.")
        }
    }
}
