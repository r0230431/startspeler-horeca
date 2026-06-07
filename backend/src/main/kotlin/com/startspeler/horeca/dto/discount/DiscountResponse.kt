package com.startspeler.horeca.dto.discount

import com.startspeler.horeca.database.enums.DiscountType
import kotlinx.serialization.Serializable

@Serializable
data class DiscountResponse(
    val id: Int,
    val name: String,
    val description: String?,
    val discountType: DiscountType,
    val discountValue: String,
    val isActive: Boolean
)