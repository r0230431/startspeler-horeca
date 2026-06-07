package com.startspeler.horeca.data.models.products

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageUploadResponse(
    val imageUrl: String,
    val originalFileName: String
)
