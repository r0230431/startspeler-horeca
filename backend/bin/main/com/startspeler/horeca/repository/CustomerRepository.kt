package com.startspeler.horeca.repository

import com.startspeler.horeca.database.tables.CustomersTable
import com.startspeler.horeca.database.tables.OrdersTable
import com.startspeler.horeca.dto.customer.CustomerResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.deleteAll
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.lowerCase
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.TransactionManager
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CustomerRepository {

    fun findAll(): List<CustomerResponse> = transaction {
        CustomersTable
            .selectAll()
            .orderBy(CustomersTable.username to SortOrder.ASC)
            .map { it.toCustomerResponse() }
    }

    fun findById(id: Int): CustomerResponse? = transaction {
        CustomersTable
            .selectAll()
            .where { CustomersTable.id eq id }
            .map { it.toCustomerResponse() }
            .singleOrNull()
    }

    fun findByName(name: String): CustomerResponse? = transaction {
        CustomersTable
            .selectAll()
            .where { CustomersTable.username eq name }
            .map { it.toCustomerResponse() }
            .singleOrNull()
    }

    fun searchByName(query: String): List<CustomerResponse> = transaction {
        val trimmedQuery = query.trim()
        if (trimmedQuery.isBlank()) return@transaction emptyList()

        CustomersTable
            .selectAll()
            .where { CustomersTable.username.lowerCase() like "%${trimmedQuery.lowercase()}%" }
            .orderBy(CustomersTable.username to SortOrder.ASC)
            .map { it.toCustomerResponse() }
    }

    fun create(name: String): CustomerResponse = transaction {
        val id = CustomersTable.insertAndGetId {
            it[CustomersTable.username] = name
        }.value

        findById(id)!!
    }

    fun update(id: Int, name: String): Boolean = transaction {
        CustomersTable.update({ CustomersTable.id eq id }) {
            it[CustomersTable.username] = name
        } > 0
    }

    fun clearCustomerReferences(customerId: Int): Int = transaction {
        OrdersTable.update({ OrdersTable.customerId eq customerId }) {
            it[OrdersTable.customerId] = null
        }
    }

    fun clearAllCustomerReferences(): Int = transaction {
        OrdersTable.update({ OrdersTable.customerId.isNotNull() }) {
            it[OrdersTable.customerId] = null
        }
    }

    fun delete(id: Int): Boolean = transaction {
        CustomersTable.deleteWhere { CustomersTable.id eq id } > 0
    }

    fun deleteAllAndResetIds(): Unit = transaction {
        CustomersTable.deleteAll()
        TransactionManager.current().exec("ALTER TABLE customers AUTO_INCREMENT = 1")
    }

    fun hasOrdersLinked(customerId: Int): Boolean = transaction {
        OrdersTable
            .selectAll()
            .andWhere { OrdersTable.customerId eq customerId }
            .limit(1)
            .any()
    }

    private fun ResultRow.toCustomerResponse(): CustomerResponse {
        return CustomerResponse(
            id = this[CustomersTable.id].value,
            username = this[CustomersTable.username]
        )
    }
}