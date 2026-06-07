package com.startspeler.horeca.repository

import com.startspeler.horeca.database.tables.PaymentsTable
import com.startspeler.horeca.database.tables.PaymentDiscountsTable
import com.startspeler.horeca.dto.payment.PaymentResponse
import com.startspeler.horeca.util.DateTimeProvider
import com.startspeler.horeca.database.enums.PaymentMethod
import com.startspeler.horeca.database.tables.DiscountsTable
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.sum
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.dao.id.EntityID
import org.jetbrains.exposed.sql.andWhere
import java.math.BigDecimal
import java.time.LocalDateTime

class PaymentRepository {

    fun findById(id: Int): ResultRow? = transaction {
        PaymentsTable
            .selectAll()
            .where { PaymentsTable.id eq id }
            .singleOrNull()
    }

    fun findAllFiltered(
        paidAtFromUtc: LocalDateTime? = null,
        paidAtToUtc: LocalDateTime? = null,
        paymentMethod: PaymentMethod? = null
    ): List<ResultRow> = transaction {
        var query = PaymentsTable.selectAll()

        if (paidAtFromUtc != null) {
            query = query.andWhere { PaymentsTable.paidAt greaterEq paidAtFromUtc }
        }

        if (paidAtToUtc != null) {
            query = query.andWhere { PaymentsTable.paidAt lessEq paidAtToUtc }
        }

        if (paymentMethod != null) {
            query = query.andWhere { PaymentsTable.paymentMethod eq paymentMethod }
        }

        query
            .orderBy(PaymentsTable.paidAt to SortOrder.DESC)
            .toList()
    }

    fun create(
        paymentMethod: PaymentMethod,
        subtotalAmount: BigDecimal,
        discountAmount: BigDecimal,
        totalAmount: BigDecimal,
    ): Int = transaction {
        val paidAtUtc = DateTimeProvider.nowUtc()

        PaymentsTable.insertAndGetId {
            it[PaymentsTable.paymentMethod] = paymentMethod
            it[PaymentsTable.subtotalAmount] = subtotalAmount
            it[PaymentsTable.discountAmount] = discountAmount
            it[PaymentsTable.totalAmount] = totalAmount
            it[PaymentsTable.paidAt] = paidAtUtc
        }.value
    }

    fun linkDiscounts(paymentId: Int, discountIds: List<Int>) = transaction {
        discountIds.forEach { discountId ->
            PaymentDiscountsTable.insert {
                it[PaymentDiscountsTable.paymentId] = EntityID(paymentId, PaymentsTable)
                it[PaymentDiscountsTable.discountId] = EntityID(discountId, DiscountsTable)
            }
        }
    }

    fun findDiscountIdsByPaymentId(paymentId: Int): List<Int> = transaction {
        PaymentDiscountsTable
            .selectAll()
            .where { PaymentDiscountsTable.paymentId eq paymentId }
            .map { it[PaymentDiscountsTable.discountId].value }
    }

    fun toPaymentResponse(row: ResultRow, orderIds: List<Int>): PaymentResponse {
        val paymentId = row[PaymentsTable.id].value
        val discountIds = findDiscountIdsByPaymentId(paymentId)
        return PaymentResponse(
            id = paymentId,
            paymentMethod = row[PaymentsTable.paymentMethod],
            orderIds = orderIds,
            discountIds = discountIds,
            subtotalAmount = row[PaymentsTable.subtotalAmount].toPlainString(),
            discountAmount = row[PaymentsTable.discountAmount].toPlainString(),
            totalAmount = row[PaymentsTable.totalAmount].toPlainString(),
            paidAt = DateTimeProvider.utcToBrussels(row[PaymentsTable.paidAt]).toString()
        )
    }

    fun getSummary(
        paidAtFromUtc: LocalDateTime? = null,
        paidAtToUtc: LocalDateTime? = null
    ): List<Pair<PaymentMethod, BigDecimal>> = transaction {
        var query = PaymentsTable
            .select(PaymentsTable.paymentMethod, PaymentsTable.totalAmount.sum())

        if (paidAtFromUtc != null) {
            query = query.andWhere { PaymentsTable.paidAt greaterEq paidAtFromUtc }
        }

        if (paidAtToUtc != null) {
            query = query.andWhere { PaymentsTable.paidAt lessEq paidAtToUtc }
        }

        query
            .groupBy(PaymentsTable.paymentMethod)
            .orderBy(PaymentsTable.paymentMethod to SortOrder.ASC)
            .map { row ->
                val paymentMethod = row[PaymentsTable.paymentMethod]
                val total = row[PaymentsTable.totalAmount.sum()] ?: BigDecimal.ZERO
                paymentMethod to total
            }
    }
}