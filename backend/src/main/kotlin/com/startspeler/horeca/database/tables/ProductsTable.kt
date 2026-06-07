package com.startspeler.horeca.database.tables
import org.jetbrains.exposed.dao.id.IntIdTable

object ProductsTable : IntIdTable(name = "products") {
    val categoryId = reference("category_id", CategoriesTable).index()
    val name = varchar("name", length = 150).uniqueIndex()
    val description = varchar("description", length = 255).nullable()
    val stock = integer("stock")
    val minimumStock = integer("minimum_stock").default(0)
    val price = decimal("price", precision = 10, scale = 2)
    val imageUrl = varchar("image_url", length = 500).nullable()
    val isActive = bool("is_active").default(true)
}
