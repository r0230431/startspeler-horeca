package com.startspeler.horeca.database.tables
import org.jetbrains.exposed.dao.id.IntIdTable

object CategoriesTable : IntIdTable(name = "categories") {
    val name = varchar("name", length = 100).uniqueIndex()
    val description = varchar("description", length = 255).nullable()
    val displayOrder = integer("display_order").uniqueIndex()
    val isActive = bool("is_active").default(true)
}