package com.startspeler.horeca.dto.payment

import com.startspeler.horeca.database.enums.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class PaymentResponse(
    val id: Int,
    val paymentMethod: PaymentMethod,
    val orderIds: List<Int>,
    val discountIds: List<Int>,
    val subtotalAmount: String,
    val discountAmount: String,
    val totalAmount: String,
    val paidAt: String
)