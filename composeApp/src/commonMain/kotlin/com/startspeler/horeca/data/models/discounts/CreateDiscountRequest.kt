package com.startspeler.horeca.data.models.discounts

import kotlinx.serialization.Serializable

@Serializable
data class CreateDiscountRequest(
    val name: String,
    val description: String? = null,
    val discountType: DiscountType,
    val discountValue: String,
    val isActive: Boolean = true,
)
