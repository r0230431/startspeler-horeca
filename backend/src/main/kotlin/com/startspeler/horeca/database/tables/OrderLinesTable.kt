package com.startspeler.horeca.database.tables
import org.jetbrains.exposed.dao.id.IntIdTable

object OrderLinesTable : IntIdTable(name = "order_lines") {
    val orderId = reference("order_id", OrdersTable).index()
    val productId = reference("product_id", ProductsTable).nullable().index()
    val productNameSnapshot = varchar("product_name_snapshot", length = 150)
    val unitPriceSnapshot = decimal("unit_price_snapshot", precision = 10, scale = 2)
    val quantity = integer("quantity")
}