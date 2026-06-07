package com.startspeler.horeca.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductInventoryRequest(
    val stock: Int,
    val minimumStock: Int
)
