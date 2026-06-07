package com.startspeler.horeca.database.tables
import com.startspeler.horeca.database.enums.CrewRole
import org.jetbrains.exposed.dao.id.IntIdTable

object CrewMembersTable : IntIdTable(name = "crew_members") {
    val username = varchar("username", length = 100).uniqueIndex()
    val passwordHash = varchar("password_hash", length = 255)
    val role = enumerationByName("role", length = 20, klass = CrewRole::class)
}
