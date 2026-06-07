package com.startspeler.horeca.data.models.discounts

import kotlinx.serialization.Serializable

@Serializable
data class DiscountResponse(
    val id: Int,
    val name: String,
    val description: String? = null,
    val discountType: DiscountType,
    val discountValue: String,
    val isActive: Boolean
)
