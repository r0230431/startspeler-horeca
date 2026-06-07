package com.startspeler.horeca.data.models.discounts

import kotlinx.serialization.Serializable

@Serializable
enum class DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
}

val DiscountType.displayLabel: String
    get() = when (this) {
        DiscountType.PERCENTAGE -> "Percentage"
        DiscountType.FIXED_AMOUNT -> "Vast bedrag"
    }

val DiscountType.valueFieldLabel: String
    get() = when (this) {
        DiscountType.PERCENTAGE -> "Percentage"
        DiscountType.FIXED_AMOUNT -> "Bedrag"
    }
