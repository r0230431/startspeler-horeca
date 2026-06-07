package com.startspeler.horeca.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class AddProductDeliveryRequest(
    val quantity: Int
)
