package com.startspeler.horeca.dto.order

import com.startspeler.horeca.database.enums.OrderStatus
import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderStatusRequest(
    val status: OrderStatus
)