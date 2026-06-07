package com.startspeler.horeca.data.models.orders

import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderRequest(
    val customerId: Int?,
    val orderedByName: String,
    val tableId: Int,
    val note: String? = null,
    val orderSource: String,
    val lines: List<OrderLineRequest>
)
