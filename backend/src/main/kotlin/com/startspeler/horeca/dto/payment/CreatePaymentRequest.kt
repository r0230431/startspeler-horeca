package com.startspeler.horeca.dto.payment

import com.startspeler.horeca.database.enums.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class CreatePaymentRequest(
    val paymentMethod: PaymentMethod,
    val orderIds: List<Int>,
    val discountIds: List<Int> = emptyList()
)