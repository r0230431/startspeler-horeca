package com.startspeler.horeca.repository

import com.startspeler.horeca.database.tables.CategoriesTable
import com.startspeler.horeca.database.tables.ProductsTable
import com.startspeler.horeca.dto.category.CategoryResponse
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update

class CategoryRepository {

    fun findAll(): List<CategoryResponse> = transaction {
        CategoriesTable
            .selectAll()
            .orderBy(CategoriesTable.displayOrder to SortOrder.ASC)
            .map { it.toCategoryResponse() }
    }

    fun findById(id: Int): CategoryResponse? = transaction {
        CategoriesTable
            .selectAll()
            .where { CategoriesTable.id eq id }
            .map { it.toCategoryResponse() }
            .singleOrNull()
    }

    fun findByName(name: String): CategoryResponse? = transaction {
        CategoriesTable
            .selectAll()
            .where { CategoriesTable.name eq name }
            .map { it.toCategoryResponse() }
            .singleOrNull()
    }

    fun findByDisplayOrder(displayOrder: Int): CategoryResponse? = transaction {
        CategoriesTable
            .selectAll()
            .where { CategoriesTable.displayOrder eq displayOrder }
            .map { it.toCategoryResponse() }
            .singleOrNull()
    }

    fun hasProducts(id: Int): Boolean = transaction {
        ProductsTable
            .selectAll()
            .where { ProductsTable.categoryId eq id }
            .count() > 0
    }

    fun create(
        name: String,
        description: String?,
        displayOrder: Int,
        isActive: Boolean
    ): CategoryResponse = transaction {
        val id = CategoriesTable.insertAndGetId {
            it[CategoriesTable.name] = name
            it[CategoriesTable.description] = description
            it[CategoriesTable.displayOrder] = displayOrder
            it[CategoriesTable.isActive] = isActive
        }.value

        findById(id)!!
    }

    fun update(
        id: Int,
        name: String,
        description: String?,
        displayOrder: Int,
        isActive: Boolean
    ): Boolean = transaction {
        CategoriesTable.update({ CategoriesTable.id eq id }) {
            it[CategoriesTable.name] = name
            it[CategoriesTable.description] = description
            it[CategoriesTable.displayOrder] = displayOrder
            it[CategoriesTable.isActive] = isActive
        } > 0
    }

    fun delete(id: Int): Boolean = transaction {
        CategoriesTable.deleteWhere { CategoriesTable.id eq id } > 0
    }

    fun deleteSafely(id: Int): Boolean {
        return try {
            delete(id)
        } catch (_: ExposedSQLException) {
            false
        }
    }

    private fun ResultRow.toCategoryResponse(): CategoryResponse {
        return CategoryResponse(
            id = this[CategoriesTable.id].value,
            name = this[CategoriesTable.name],
            description = this[CategoriesTable.description],
            displayOrder = this[CategoriesTable.displayOrder],
            isActive = this[CategoriesTable.isActive]
        )
    }
}