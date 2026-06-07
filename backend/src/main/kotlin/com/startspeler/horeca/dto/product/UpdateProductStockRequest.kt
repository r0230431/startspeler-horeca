package com.startspeler.horeca.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductStockRequest(
    val stock: Int
)
