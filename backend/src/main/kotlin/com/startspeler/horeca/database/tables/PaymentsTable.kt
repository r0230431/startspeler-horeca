package com.startspeler.horeca.database.tables
import com.startspeler.horeca.database.enums.PaymentMethod
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object PaymentsTable : IntIdTable(name = "payments") {
    val paymentMethod = enumerationByName("payment_method", length = 20, klass = PaymentMethod::class)
    val subtotalAmount = decimal("subtotal_amount", precision = 10, scale = 2)
    val discountAmount = decimal("discount_amount", precision = 10, scale = 2).default(java.math.BigDecimal.ZERO)
    val totalAmount = decimal("total_amount", precision = 10, scale = 2)
    val paidAt = datetime("paid_at")
}