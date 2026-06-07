package com.startspeler.horeca.data.models.products

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductInventoryRequest(
    val stock: Int,
    val minimumStock: Int
)
