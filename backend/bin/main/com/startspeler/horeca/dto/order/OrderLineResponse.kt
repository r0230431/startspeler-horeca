package com.startspeler.horeca.dto.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineResponse(
    val id: Int,
    val productId: Int?,
    val productNameSnapshot: String,
    val unitPriceSnapshot: Double,
    val quantity: Int
)