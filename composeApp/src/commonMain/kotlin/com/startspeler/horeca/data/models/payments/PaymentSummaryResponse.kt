package com.startspeler.horeca.data.models.payments

import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodTotalResponse(
    val paymentMethod: String,
    val totalAmount: String
)

@Serializable
data class PaymentSummaryResponse(
    val totalsByMethod: List<PaymentMethodTotalResponse> = emptyList(),
    val grandTotal: String,
    val from: String? = null,
    val to: String? = null
)
