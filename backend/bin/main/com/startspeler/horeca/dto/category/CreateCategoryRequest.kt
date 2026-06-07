package com.startspeler.horeca.dto.category

import kotlinx.serialization.Serializable

@Serializable
data class CreateCategoryRequest(
    val name: String,
    val description: String? = null,
    val displayOrder: Int,
    val isActive: Boolean = true
)