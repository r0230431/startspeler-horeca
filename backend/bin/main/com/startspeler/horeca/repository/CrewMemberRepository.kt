package com.startspeler.horeca.repository

import com.startspeler.horeca.database.enums.CrewRole
import com.startspeler.horeca.database.tables.CrewMembersTable
import com.startspeler.horeca.dto.crew.CrewMemberResponse
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

data class CrewMemberAuthModel(
    val id: Int,
    val username: String,
    val passwordHash: String,
    val role: CrewRole,
)

class CrewMemberRepository {

    fun findAll(): List<CrewMemberResponse> = transaction {
        CrewMembersTable
            .selectAll()
            .orderBy(CrewMembersTable.username to SortOrder.ASC)
            .map { it.toCrewMemberResponse() }
    }

    fun findById(id: Int): CrewMemberResponse? = transaction {
        CrewMembersTable
            .selectAll()
            .where { CrewMembersTable.id eq id }
            .map { it.toCrewMemberResponse() }
            .singleOrNull()
    }

    fun findByUsername(username: String): CrewMemberAuthModel? = transaction {
        CrewMembersTable
            .select(
                CrewMembersTable.id,
                CrewMembersTable.username,
                CrewMembersTable.passwordHash,
                CrewMembersTable.role
            )
            .where { CrewMembersTable.username eq username }
            .singleOrNull()
            ?.let {
                CrewMemberAuthModel(
                    id = it[CrewMembersTable.id].value,
                    username = it[CrewMembersTable.username],
                    passwordHash = it[CrewMembersTable.passwordHash],
                    role = it[CrewMembersTable.role],
                )
            }
    }

    fun create(
        username: String,
        passwordHash: String,
        role: CrewRole
    ): CrewMemberResponse = transaction {
        val id = CrewMembersTable.insertAndGetId {
            it[CrewMembersTable.username] = username
            it[CrewMembersTable.passwordHash] = passwordHash
            it[CrewMembersTable.role] = role
        }.value

        findById(id)!!
    }

    fun update(
        id: Int,
        username: String,
        role: CrewRole,
        passwordHash: String? = null,
    ): Boolean = transaction {
        CrewMembersTable.update({ CrewMembersTable.id eq id }) {
            it[CrewMembersTable.username] = username
            it[CrewMembersTable.role] = role
            if (passwordHash != null) {
                it[CrewMembersTable.passwordHash] = passwordHash
            }
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        CrewMembersTable.deleteWhere { CrewMembersTable.id eq id } > 0
    }

    fun updatePasswordHash(
        crewMemberId: Int,
        newPasswordHash: String
    ): Boolean = transaction {
        CrewMembersTable.update({ CrewMembersTable.id eq crewMemberId }) {
            it[passwordHash] = newPasswordHash
        } > 0
    }

    fun findAuthById(crewMemberId: Int): CrewMemberAuthModel? = transaction {
        CrewMembersTable
            .select(
                CrewMembersTable.id,
                CrewMembersTable.username,
                CrewMembersTable.passwordHash,
                CrewMembersTable.role,
            )
            .where { CrewMembersTable.id eq crewMemberId }
            .singleOrNull()
            ?.let {
                CrewMemberAuthModel(
                    id = it[CrewMembersTable.id].value,
                    username = it[CrewMembersTable.username],
                    passwordHash = it[CrewMembersTable.passwordHash],
                    role = it[CrewMembersTable.role],
                )
            }
    }

    private fun ResultRow.toCrewMemberResponse(): CrewMemberResponse {
        return CrewMemberResponse(
            id = this[CrewMembersTable.id].value,
            username = this[CrewMembersTable.username],
            role = this[CrewMembersTable.role]
        )
    }

    fun countAdmins(): Long = transaction {
        CrewMembersTable
            .selectAll()
            .where { CrewMembersTable.role eq CrewRole.ADMIN }
            .count()
    }
}