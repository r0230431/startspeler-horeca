package com.startspeler.horeca.dto.order

import com.startspeler.horeca.database.enums.OrderSource
import com.startspeler.horeca.database.enums.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class OrderResponse(
    val id: Int,
    val customerId: Int?,
    val orderedByName: String,
    val tableId: Int,
    val status: OrderStatus,
    val note: String?,
    val createdAt: String,
    val paymentId: Int?,
    val orderSource: OrderSource,
    val lines: List<OrderLineResponse>
)