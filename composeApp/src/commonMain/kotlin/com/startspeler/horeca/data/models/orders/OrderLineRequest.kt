package com.startspeler.horeca.data.models.orders

import kotlinx.serialization.Serializable

@Serializable
data class OrderLineRequest(
    val productId: Int,
    val quantity: Int
)