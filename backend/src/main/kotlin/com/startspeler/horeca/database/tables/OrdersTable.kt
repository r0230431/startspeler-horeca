package com.startspeler.horeca.database.tables
import com.startspeler.horeca.database.enums.OrderSource
import com.startspeler.horeca.database.enums.OrderStatus
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.javatime.datetime

object OrdersTable : IntIdTable(name = "orders") {
    val customerId = reference("customer_id", CustomersTable).nullable().index()
    val orderedByName = varchar("ordered_by_name", length = 100)
    val tableId = reference("table_id", CafeTablesTable).index()
    val status = enumerationByName("status", length = 20, klass = OrderStatus::class).index()
    val note = varchar("note", length = 500).nullable()
    val createdAt = datetime("created_at").index()
    val paymentId = reference("payment_id", PaymentsTable).nullable().index()
    val orderSource = enumerationByName("order_source", length = 20, klass = OrderSource::class)
}