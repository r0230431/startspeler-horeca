package com.startspeler.horeca.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val description: String?,
    val stock: Int,
    val minimumStock: Int,
    val price: String,
    val imageUrl: String?,
    val isActive: Boolean
)