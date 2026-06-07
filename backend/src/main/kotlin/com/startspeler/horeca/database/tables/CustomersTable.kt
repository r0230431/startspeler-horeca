package com.startspeler.horeca.database.tables
import org.jetbrains.exposed.dao.id.IntIdTable

object CustomersTable : IntIdTable(name = "customers") {
    val username = varchar("username", length = 100).uniqueIndex()
}
