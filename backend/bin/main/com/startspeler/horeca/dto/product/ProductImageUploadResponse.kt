package com.startspeler.horeca.dto.product

import kotlinx.serialization.Serializable

@Serializable
data class ProductImageUploadResponse(
    val imageUrl: String,
    val originalFileName: String
)
