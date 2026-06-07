package com.startspeler.horeca.data.models.orders

import kotlinx.serialization.Serializable

@Serializable
data class UpdateOrderStatusRequest(
    val status: String
)