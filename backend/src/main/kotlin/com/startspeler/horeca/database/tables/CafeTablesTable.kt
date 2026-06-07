package com.startspeler.horeca.database.tables
import org.jetbrains.exposed.dao.id.IntIdTable

object CafeTablesTable : IntIdTable(name = "cafe_tables") {
    val number = integer("number").uniqueIndex()
    val seatCount = integer("seat_count")
    val note = varchar("note", length = 255).nullable()
}