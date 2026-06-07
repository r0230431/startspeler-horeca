package com.startspeler.horeca.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class UpdateProductRequest(
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val stock: Int,
    val minimumStock: Int = 0,
    val price: String,
    val imageUrl: String? = null,
    val isActive: Boolean
)