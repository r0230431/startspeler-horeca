package com.startspeler.horeca.dto.order

import com.startspeler.horeca.database.enums.OrderSource
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderRequest(
    val customerId: Int?,
    val orderedByName: String,
    val tableId: Int,
    val note: String? = null,
    val orderSource: OrderSource,
    val lines: List<OrderLineRequest>
)