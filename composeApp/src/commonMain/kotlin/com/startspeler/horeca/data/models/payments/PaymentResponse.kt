package com.startspeler.horeca.data.models.payments

import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val id: Int,
    val paymentMethod: String,
    val orderIds: List<Int>,
    val discountIds: List<Int> = emptyList(),
    val subtotalAmount: String,
    val discountAmount: String,
    val totalAmount: String,
    val paidAt: String
)
