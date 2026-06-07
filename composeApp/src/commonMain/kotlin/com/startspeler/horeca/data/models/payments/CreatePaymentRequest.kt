package com.startspeler.horeca.data.models.payments

import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    val paymentMethod: String,
    val orderIds: List<Int>,
    val discountIds: List<Int> = emptyList()
)
