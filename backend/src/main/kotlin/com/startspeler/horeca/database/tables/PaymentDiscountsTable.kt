package com.startspeler.horeca.database.tables

import org.jetbrains.exposed.dao.id.IntIdTable

object PaymentDiscountsTable : IntIdTable(name = "payment_discounts") {
    val paymentId = reference("payment_id", PaymentsTable)
    val discountId = reference("discount_id", DiscountsTable)
}
