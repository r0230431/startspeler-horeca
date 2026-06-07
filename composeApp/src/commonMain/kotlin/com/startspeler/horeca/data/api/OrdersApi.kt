package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.orders.CreateOrderRequest
import com.startspeler.horeca.data.models.orders.OrderResponse
import com.startspeler.horeca.data.models.orders.UpdateOrderRequest
import com.startspeler.horeca.data.models.orders.UpdateOrderStatusRequest
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.patch
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class OrdersApi {
    suspend fun getOrders(): List<OrderResponse> {
        return HttpClientProvider.client.get("/orders").body()
    }

    suspend fun getOrdersFiltered(
        status: String? = null,
        onlyUnpaid: Boolean? = null,
        customerId: Int? = null,
        orderedByName: String? = null
    ): List<OrderResponse> {
        return HttpClientProvider.client.get("/orders") {
            status?.let { parameter("status", it) }
            onlyUnpaid?.let { parameter("onlyUnpaid", it) }
            customerId?.let { parameter("customerId", it) }
            orderedByName?.takeIf { it.isNotBlank() }?.let { parameter("orderedByName", it) }
        }.body()
    }

    suspend fun getOrderById(orderId: Int): ApiResult<OrderResponse> {
        return try {
            val response = HttpClientProvider.client.get("/orders/$orderId")
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(extractApiMessage(response.bodyAsText(), "Bestelling kon niet worden opgehaald."))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij ophalen van bestelling.")
        }
    }

    suspend fun createOrder(request: CreateOrderRequest): ApiResult<OrderResponse> {
        return try {
            val response = HttpClientProvider.client.post("/orders") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(response.bodyAsText(), "Bestelling kon niet worden opgeslagen.")
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij opslaan van bestelling.")
        }
    }

    suspend fun updateOrder(orderId: Int, request: UpdateOrderRequest): ApiResult<OrderResponse> {
        return try {
            val response = HttpClientProvider.client.put("/orders/$orderId") {
                setBody(request)
            }
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(extractApiMessage(response.bodyAsText(), "Bestelling kon niet worden bijgewerkt."))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van bestelling.")
        }
    }

    suspend fun updateOrderStatus(
        orderId: Int,
        status: String
    ): ApiResult<OrderResponse> {
        return try {
            val response = HttpClientProvider.client.patch("/orders/$orderId/status") {
                setBody(UpdateOrderStatusRequest(status = status))
            }
            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(extractApiMessage(response.bodyAsText(), "Bestelstatus kon niet worden aangepast."))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij aanpassen van bestelstatus.")
        }
    }
}