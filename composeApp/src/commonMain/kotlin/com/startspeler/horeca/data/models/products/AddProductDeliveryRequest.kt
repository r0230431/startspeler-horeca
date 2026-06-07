package com.startspeler.horeca.data.models.products

import kotlinx.serialization.Serializable

@Serializable
data class AddProductDeliveryRequest(
    val quantity: Int
)
