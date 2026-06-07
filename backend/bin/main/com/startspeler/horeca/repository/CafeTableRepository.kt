package com.startspeler.horeca.repository

import com.startspeler.horeca.database.tables.CafeTablesTable
import com.startspeler.horeca.dto.table.TableResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CafeTableRepository {

    fun findAll(): List<TableResponse> = transaction {
        CafeTablesTable
            .selectAll()
            .orderBy(CafeTablesTable.number to SortOrder.ASC)
            .map { it.toTableResponse() }
    }

    fun findById(id: Int): TableResponse? = transaction {
        CafeTablesTable
            .selectAll()
            .where { CafeTablesTable.id eq id }
            .map { it.toTableResponse() }
            .singleOrNull()
    }

    fun findByTableNumber(tableNumber: Int): TableResponse? = transaction {
        CafeTablesTable
            .selectAll()
            .where { CafeTablesTable.number eq tableNumber }
            .map { it.toTableResponse() }
            .singleOrNull()
    }

    fun create(
        tableNumber: Int,
        seatCount: Int,
        note: String?
    ): TableResponse = transaction {
        val id = CafeTablesTable.insertAndGetId {
            it[CafeTablesTable.number] = tableNumber
            it[CafeTablesTable.seatCount] = seatCount
            it[CafeTablesTable.note] = note
        }.value

        findById(id)!!
    }

    fun update(
        id: Int,
        tableNumber: Int,
        seatCount: Int,
        note: String?
    ): Boolean = transaction {
        CafeTablesTable.update({ CafeTablesTable.id eq id }) {
            it[CafeTablesTable.number] = tableNumber
            it[CafeTablesTable.seatCount] = seatCount
            it[CafeTablesTable.note] = note
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        CafeTablesTable.deleteWhere { CafeTablesTable.id eq id } > 0
    }

    private fun ResultRow.toTableResponse(): TableResponse {
        return TableResponse(
            id = this[CafeTablesTable.id].value,
            tableNumber = this[CafeTablesTable.number],
            seatCount = this[CafeTablesTable.seatCount],
            note = this[CafeTablesTable.note],
        )
    }
}