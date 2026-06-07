package com.startspeler.horeca.dto.discount

import com.startspeler.horeca.database.enums.DiscountType
import kotlinx.serialization.Serializable

@Serializable
data class UpdateDiscountRequest(
    val name: String,
    val description: String? = null,
    val discountType: DiscountType,
    val discountValue: String,
    val isActive: Boolean
)