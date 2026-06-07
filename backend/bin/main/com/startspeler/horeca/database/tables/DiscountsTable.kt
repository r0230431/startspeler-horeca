package com.startspeler.horeca.database.tables

import com.startspeler.horeca.database.enums.DiscountType
import org.jetbrains.exposed.dao.id.IntIdTable

object DiscountsTable : IntIdTable("discounts") {
    val name = varchar("name", 100).uniqueIndex()
    val description = varchar("description", 255).nullable()
    val discountType = enumerationByName("discount_type", 20, DiscountType::class)
    val discountValue = decimal("discount_value", precision = 10, scale = 2)
    val isActive = bool("is_active").default(true)
}