package com.startspeler.horeca.repository

import com.startspeler.horeca.database.enums.OrderSource
import com.startspeler.horeca.database.enums.OrderStatus
import com.startspeler.horeca.database.tables.OrderLinesTable
import com.startspeler.horeca.database.tables.OrdersTable
import com.startspeler.horeca.database.tables.ProductsTable
import com.startspeler.horeca.dto.order.OrderLineResponse
import com.startspeler.horeca.dto.order.OrderResponse
import com.startspeler.horeca.util.DateTimeProvider
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class OrderRepository {

    data class CreateOrderLineData(
        val productId: Int,
        val productNameSnapshot: String,
        val unitPriceSnapshot: BigDecimal,
        val quantity: Int
    )

    fun createOrder(
        customerId: Int?,
        orderedByName: String,
        tableId: Int,
        note: String?,
        orderSource: OrderSource,
        lines: List<CreateOrderLineData>
    ): OrderResponse = transaction {
        val createdAtUtc = DateTimeProvider.nowUtc()

        val orderId = OrdersTable.insertAndGetId {
            it[OrdersTable.customerId] = customerId
            it[OrdersTable.orderedByName] = orderedByName
            it[OrdersTable.tableId] = tableId
            it[OrdersTable.status] = OrderStatus.IN_PROGRESS
            it[OrdersTable.note] = note
            it[OrdersTable.createdAt] = createdAtUtc
            it[OrdersTable.paymentId] = null
            it[OrdersTable.orderSource] = orderSource
        }.value

        lines.forEach { line ->
            OrderLinesTable.insertAndGetId {
                it[OrderLinesTable.orderId] = orderId
                it[productId] = line.productId
                it[productNameSnapshot] = line.productNameSnapshot
                it[unitPriceSnapshot] = line.unitPriceSnapshot
                it[quantity] = line.quantity
            }
        }

        findById(orderId)!!
    }

    fun findById(id: Int): OrderResponse? = transaction {
        val orderRow = OrdersTable
            .selectAll()
            .andWhere { OrdersTable.id eq id }
            .singleOrNull() ?: return@transaction null

        val lines = OrderLinesTable
            .selectAll()
            .andWhere { OrderLinesTable.orderId eq id }
            .map {
                OrderLineResponse(
                    id = it[OrderLinesTable.id].value,
                    productId = it[OrderLinesTable.productId]?.value,
                    productNameSnapshot = it[OrderLinesTable.productNameSnapshot],
                    unitPriceSnapshot = it[OrderLinesTable.unitPriceSnapshot].toDouble(),
                    quantity = it[OrderLinesTable.quantity]
                )
            }

        return@transaction toOrderResponse(orderRow, lines)

//        OrderResponse(
//            id = orderRow[OrdersTable.id].value,
//            customerId = orderRow[OrdersTable.customerId]?.value,
//            orderedByName = orderRow[OrdersTable.orderedByName],
//            tableId = orderRow[OrdersTable.tableId].value,
//            status = orderRow[OrdersTable.status],
//            note = orderRow[OrdersTable.note],
//            createdAt = DateTimeProvider.utcToBrussels(orderRow[OrdersTable.createdAt]).toString(),
//            paymentId = orderRow[OrdersTable.paymentId]?.value,
//            orderSource = orderRow[OrdersTable.orderSource],
//            lines = lines
//        )
    }

    fun findAll(): List<OrderResponse> = transaction {
        OrdersTable
            .selectAll()
            .orderBy(OrdersTable.createdAt to SortOrder.DESC)
            .mapNotNull { row ->
                findById(row[OrdersTable.id].value)
            }
    }

    fun findAllFiltered(
        status: OrderStatus? = null,
        onlyUnpaid: Boolean? = null,
        tableId: Int? = null,
        customerId: Int? = null,
        orderSource: OrderSource? = null,
        orderedByName: String? = null
    ): List<OrderResponse> = transaction {
        var query = OrdersTable.selectAll()

        if (status != null) {
            query = query.andWhere { OrdersTable.status eq status }
        }

        if (onlyUnpaid == true) {
            query = query.andWhere { OrdersTable.paymentId.isNull() }
        }

        if (onlyUnpaid == false) {
            query = query.andWhere { OrdersTable.paymentId.isNotNull() }
        }

        if (tableId != null) {
            query = query.andWhere { OrdersTable.tableId eq tableId }
        }

        if (customerId != null) {
            query = query.andWhere { OrdersTable.customerId eq customerId }
        }

        if (orderSource != null) {
            query = query.andWhere { OrdersTable.orderSource eq orderSource }
        }

        if (!orderedByName.isNullOrBlank()) {
            query = query.andWhere { OrdersTable.orderedByName like "%$orderedByName%" }
        }

        query
            .orderBy(OrdersTable.createdAt to SortOrder.DESC)
            .mapNotNull { row ->
                findById(row[OrdersTable.id].value)
            }
    }

    fun updateOrder(
        id: Int,
        customerId: Int?,
        orderedByName: String,
        tableId: Int,
        note: String?,
        orderSource: OrderSource,
        lines: List<CreateOrderLineData>
    ): OrderResponse? = transaction {
        val updatedRows = OrdersTable.update({ OrdersTable.id eq id }) {
            it[OrdersTable.customerId] = customerId
            it[OrdersTable.orderedByName] = orderedByName
            it[OrdersTable.tableId] = tableId
            it[OrdersTable.note] = note
            it[OrdersTable.orderSource] = orderSource
        }

        if (updatedRows == 0) return@transaction null

        OrderLinesTable.deleteWhere { OrderLinesTable.orderId eq id }

        lines.forEach { line ->
            OrderLinesTable.insert {
                it[orderId] = id
                it[productId] = line.productId
                it[productNameSnapshot] = line.productNameSnapshot
                it[unitPriceSnapshot] = line.unitPriceSnapshot
                it[quantity] = line.quantity
            }
        }

        findById(id)
    }

//    fun updateStatus(id: Int, status: OrderStatus): Boolean = transaction {
//        OrdersTable.update({ OrdersTable.id eq id }) {
//            it[OrdersTable.status] = status
//        } > 0
//    }
//
//    data class OrderLineStockData(
//        val productId: Int?,
//        val quantity: Int
//    )
//
//     fun getOrderLinesForStock(orderId: Int): List<OrderLineStockData> = transaction {
//        OrderLinesTable
//            .selectAll()
//            .andWhere { OrderLinesTable.orderId eq orderId }
//            .map {
//                OrderLineStockData(
//                    productId = it[OrderLinesTable.productId]?.value,
//                    quantity = it[OrderLinesTable.quantity]
//                )
//            }
//    }

    fun updateStatusAndDecreaseStockIfNeeded(
        orderId: Int,
        newStatus: OrderStatus
    ): Boolean = transaction {
        OrdersTable
            .selectAll()
            .andWhere { OrdersTable.id eq orderId }
            .singleOrNull() ?: return@transaction false

        if (newStatus == OrderStatus.CANCELLED) {
            val lines = OrderLinesTable
                .selectAll()
                .andWhere { OrderLinesTable.orderId eq orderId }
                .toList()

            for (line in lines) {
                val productId = line[OrderLinesTable.productId]?.value
                    ?: return@transaction false

                val quantity = line[OrderLinesTable.quantity]

                val productRow = ProductsTable
                    .selectAll()
                    .andWhere { ProductsTable.id eq productId }
                    .singleOrNull() ?: continue

                val restoredStock = productRow[ProductsTable.stock] + quantity

                ProductsTable.update({ ProductsTable.id eq productId }) {
                    it[stock] = restoredStock
                }
            }
        }

        OrdersTable.update({ OrdersTable.id eq orderId }) {
            it[status] = newStatus
        } > 0
    }

    fun findByIds(orderIds: List<Int>): List<OrderResponse> = transaction {
        if (orderIds.isEmpty()) return@transaction emptyList()

        OrdersTable
            .selectAll()
            .andWhere { OrdersTable.id inList orderIds }
            .mapNotNull { row -> findById(row[OrdersTable.id].value) }
    }

    fun assignPaymentToOrders(paymentId: Int, orderIds: List<Int>): Boolean = transaction {
        OrdersTable.update({ OrdersTable.id inList orderIds }) {
            it[OrdersTable.paymentId] = paymentId
        } == orderIds.size
    }

    fun findOrderIdsByPaymentId(paymentId: Int): List<Int> = transaction {
        OrdersTable
            .selectAll()
            .andWhere { OrdersTable.paymentId eq paymentId }
            .map { it[OrdersTable.id].value }
    }

    private fun toOrderResponse(orderRow: ResultRow, lines: List<OrderLineResponse>): OrderResponse {
        return OrderResponse(
            id = orderRow[OrdersTable.id].value,
            customerId = orderRow[OrdersTable.customerId]?.value,
            orderedByName = orderRow[OrdersTable.orderedByName],
            tableId = orderRow[OrdersTable.tableId].value,
            status = orderRow[OrdersTable.status],
            note = orderRow[OrdersTable.note],
            createdAt = DateTimeProvider.utcToBrussels(orderRow[OrdersTable.createdAt]).toString(),
            paymentId = orderRow[OrdersTable.paymentId]?.value,
            orderSource = orderRow[OrdersTable.orderSource],
            lines = lines
        )
    }
}