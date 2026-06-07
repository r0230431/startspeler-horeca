package com.startspeler.horeca.data.models.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: Int,
    val tableId: Int,
    val tableNumber: Int? = null,
    val customerId: Int? = null,
    val orderedByName: String,
    val status: String,
    val orderSource: String,
    val note: String? = null,
    val paymentId: Int? = null,
    val createdAt: String,
    val lines: List<OrderLineResponse> = emptyList()
)