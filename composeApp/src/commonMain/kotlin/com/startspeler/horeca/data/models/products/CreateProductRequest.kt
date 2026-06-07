package com.startspeler.horeca.data.models.products

import kotlinx.serialization.Serializable

@Serializable
data class CreateProductRequest(
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val stock: Int,
    val minimumStock: Int = 0,
    val price: String,
    val imageUrl: String? = null,
    val isActive: Boolean = true
)
