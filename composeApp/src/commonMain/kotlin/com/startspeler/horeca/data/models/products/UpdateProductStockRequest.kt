package com.startspeler.horeca.data.models.products

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductStockRequest(
    val stock: Int
)
