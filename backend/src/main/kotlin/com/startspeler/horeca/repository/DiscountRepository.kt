package com.startspeler.horeca.repository

import com.startspeler.horeca.database.tables.DiscountsTable
import com.startspeler.horeca.dto.discount.DiscountResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class DiscountRepository {

    fun findAll(): List<DiscountResponse> = transaction {
        DiscountsTable
            .selectAll()
            .orderBy(DiscountsTable.name to SortOrder.ASC)
            .map { it.toDiscountResponse() }
    }

    fun findById(id: Int): DiscountResponse? = transaction {
        DiscountsTable
            .selectAll()
            .where { DiscountsTable.id eq id }
            .map { it.toDiscountResponse() }
            .singleOrNull()
    }

    fun findByName(name: String): DiscountResponse? = transaction {
        DiscountsTable
            .selectAll()
            .where { DiscountsTable.name eq name }
            .map { it.toDiscountResponse() }
            .singleOrNull()
    }

    fun create(
        name: String,
        description: String?,
        discountType: com.startspeler.horeca.database.enums.DiscountType,
        discountValue: java.math.BigDecimal,
        isActive: Boolean
    ): DiscountResponse = transaction {
        val id = DiscountsTable.insertAndGetId {
            it[DiscountsTable.name] = name
            it[DiscountsTable.description] = description
            it[DiscountsTable.discountType] = discountType
            it[DiscountsTable.discountValue] = discountValue
            it[DiscountsTable.isActive] = isActive
        }.value

        findById(id)!!
    }

    fun update(
        id: Int,
        name: String,
        description: String?,
        discountType: com.startspeler.horeca.database.enums.DiscountType,
        discountValue: java.math.BigDecimal,
        isActive: Boolean
    ): Boolean = transaction {
        DiscountsTable.update({ DiscountsTable.id eq id }) {
            it[DiscountsTable.name] = name
            it[DiscountsTable.description] = description
            it[DiscountsTable.discountType] = discountType
            it[DiscountsTable.discountValue] = discountValue
            it[DiscountsTable.isActive] = isActive
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        DiscountsTable.deleteWhere { DiscountsTable.id eq id } > 0
    }

    private fun ResultRow.toDiscountResponse(): DiscountResponse {
        return DiscountResponse(
            id = this[DiscountsTable.id].value,
            name = this[DiscountsTable.name],
            description = this[DiscountsTable.description],
            discountType = this[DiscountsTable.discountType],
            discountValue = this[DiscountsTable.discountValue].toPlainString(),
            isActive = this[DiscountsTable.isActive]
        )
    }
}