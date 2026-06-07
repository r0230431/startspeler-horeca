package com.startspeler.horeca.data.models.categories

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCategoryRequest(
    val name: String,
    val description: String? = null,
    val displayOrder: Int,
    val isActive: Boolean,
)
