package com.startspeler.horeca.data.models.products

import kotlinx.serialization.Serializable

@Serializable
data class ProductResponse(
    val id: Int,
    val categoryId: Int,
    val name: String,
    val description: String? = null,
    val stock: Int,
    val minimumStock: Int,
    val price: String,
    val imageUrl: String? = null,
    val isActive: Boolean
)