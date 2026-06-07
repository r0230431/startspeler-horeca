package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.payments.CreatePaymentRequest
import com.startspeler.horeca.data.models.payments.PaymentResponse
import com.startspeler.horeca.data.models.payments.PaymentSummaryResponse
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class PaymentsApi {
    suspend fun createPayment(request: CreatePaymentRequest): ApiResult<PaymentResponse> {
        return try {
            val response = HttpClientProvider.client.post("/payments") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(response.bodyAsText(), "Betaling kon niet worden geregistreerd.")
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij registreren van betaling.")
        }
    }

    suspend fun getPayments(
        from: String? = null,
        to: String? = null,
        paymentMethod: String? = null
    ): ApiResult<List<PaymentResponse>> {
        return try {
            val response = HttpClientProvider.client.get("/payments") {
                from?.let { parameter("from", it) }
                to?.let { parameter("to", it) }
                paymentMethod?.takeIf { it.isNotBlank() }?.let { parameter("paymentMethod", it) }
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(extractApiMessage(response.bodyAsText(), "Betalingen konden niet worden opgehaald."))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij ophalen van betalingen.")
        }
    }

    suspend fun getPaymentSummary(from: String? = null, to: String? = null): ApiResult<PaymentSummaryResponse> {
        return try {
            val response = HttpClientProvider.client.get("/payments/summary") {
                from?.let { parameter("from", it) }
                to?.let { parameter("to", it) }
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(extractApiMessage(response.bodyAsText(), "Kassaoverzicht kon niet worden opgehaald."))
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij ophalen van kassaoverzicht.")
        }
    }
}
