package com.startspeler.horeca.data.models.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineResponse(
    val id: Int? = null,
    val productId: Int? = null,
    val productNameSnapshot: String,
    val quantity: Int,
    val unitPriceSnapshot: Double? = null,
    val lineTotal: Double? = null
)