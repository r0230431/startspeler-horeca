package com.startspeler.horeca.dto.payment

import com.startspeler.horeca.database.enums.PaymentMethod
import kotlinx.serialization.Serializable

@Serializable
data class PaymentMethodTotalResponse(
    val paymentMethod: PaymentMethod,
    val totalAmount: String
)

@Serializable
data class PaymentSummaryResponse(
    val totalsByMethod: List<PaymentMethodTotalResponse>,
    val grandTotal: String,
    val from: String? = null,
    val to: String? = null
)