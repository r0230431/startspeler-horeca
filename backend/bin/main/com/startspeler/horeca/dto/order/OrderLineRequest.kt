package com.startspeler.horeca.dto.order

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineRequest(
    val productId: Int,
    val quantity: Int
)