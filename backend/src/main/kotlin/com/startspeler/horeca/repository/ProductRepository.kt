package com.startspeler.horeca.repository

import com.startspeler.horeca.database.tables.CategoriesTable
import com.startspeler.horeca.database.tables.ProductsTable
import com.startspeler.horeca.dto.product.ProductResponse
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SqlExpressionBuilder
import org.jetbrains.exposed.sql.SqlExpressionBuilder.eq
import org.jetbrains.exposed.sql.SqlExpressionBuilder.greaterEq
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.deleteWhere
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import org.jetbrains.exposed.sql.update
import java.math.BigDecimal

class ProductRepository {

    fun findAll(): List<ProductResponse> = transaction {
        ProductsTable
            .selectAll()
            .orderBy(ProductsTable.name to SortOrder.ASC)
            .map { it.toProductResponse() }
    }

    fun findAllActive(): List<ProductResponse> = transaction {
        ProductsTable
            .selectAll()
            .where { ProductsTable.isActive eq true }
            .orderBy(ProductsTable.name to SortOrder.ASC)
            .map { it.toProductResponse() }
    }

    fun findById(id: Int): ProductResponse? = transaction {
        ProductsTable
            .selectAll()
            .where { ProductsTable.id eq id }
            .map { it.toProductResponse() }
            .singleOrNull()
    }

    fun findByName(name: String): ProductResponse? = transaction {
        ProductsTable
            .selectAll()
            .where { ProductsTable.name eq name }
            .map { it.toProductResponse() }
            .singleOrNull()
    }

    fun categoryExists(categoryId: Int): Boolean = transaction {
        CategoriesTable
            .selectAll()
            .where { CategoriesTable.id eq categoryId }
            .count() > 0
    }

    fun create(
        categoryId: Int,
        name: String,
        description: String?,
        stock: Int,
        minimumStock: Int,
        price: BigDecimal,
        imageUrl: String?,
        isActive: Boolean
    ): ProductResponse = transaction {
        val id = ProductsTable.insertAndGetId {
            it[ProductsTable.categoryId] = categoryId
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.stock] = stock
            it[ProductsTable.minimumStock] = minimumStock
            it[ProductsTable.price] = price
            it[ProductsTable.imageUrl] = imageUrl
            it[ProductsTable.isActive] = isActive
        }.value

        findById(id)!!
    }

    fun update(
        id: Int,
        categoryId: Int,
        name: String,
        description: String?,
        stock: Int,
        minimumStock: Int,
        price: BigDecimal,
        imageUrl: String?,
        isActive: Boolean
    ): Boolean = transaction {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[ProductsTable.categoryId] = categoryId
            it[ProductsTable.name] = name
            it[ProductsTable.description] = description
            it[ProductsTable.stock] = stock
            it[ProductsTable.minimumStock] = minimumStock
            it[ProductsTable.price] = price
            it[ProductsTable.imageUrl] = imageUrl
            it[ProductsTable.isActive] = isActive
        } > 0
    }

    fun updateStock(id: Int, stock: Int): Boolean = transaction {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[ProductsTable.stock] = stock
        } > 0
    }

    fun updateInventory(id: Int, stock: Int, minimumStock: Int): Boolean = transaction {
        ProductsTable.update({ ProductsTable.id eq id }) {
            it[ProductsTable.stock] = stock
            it[ProductsTable.minimumStock] = minimumStock
        } > 0
    }

    fun addDelivery(id: Int, quantity: Int): ProductResponse? = transaction {
        val productRow = ProductsTable
            .selectAll()
            .where { ProductsTable.id eq id }
            .singleOrNull() ?: return@transaction null

        val newStock = productRow[ProductsTable.stock] + quantity
        val updated = ProductsTable.update({ ProductsTable.id eq id }) {
            it[stock] = newStock
        }

        if (updated == 0) return@transaction null

        ProductsTable
            .selectAll()
            .where { ProductsTable.id eq id }
            .map { it.toProductResponse() }
            .singleOrNull()
    }

    fun decreaseStock(productId: Int, quantity: Int): Boolean = transaction {
        ProductsTable.update({
            (ProductsTable.id eq productId) and (ProductsTable.stock greaterEq quantity)
        }) {
            with(SqlExpressionBuilder) {
                it[stock] = stock - quantity
            }
        } > 0
    }

    fun restoreStock(productId: Int, quantity: Int) = transaction {
        ProductsTable.update({ ProductsTable.id eq productId }) {
            with(SqlExpressionBuilder) {
                it[stock] = stock + quantity
            }
        }
    }

    fun delete(id: Int): Boolean = transaction {
        ProductsTable.deleteWhere { ProductsTable.id eq id } > 0
    }

    fun deleteSafely(id: Int): Boolean {
        return try {
            delete(id)
        } catch (_: ExposedSQLException) {
            false
        }
    }

    private fun ResultRow.toProductResponse(): ProductResponse {
        return ProductResponse(
            id = this[ProductsTable.id].value,
            categoryId = this[ProductsTable.categoryId].value,
            name = this[ProductsTable.name],
            description = this[ProductsTable.description],
            stock = this[ProductsTable.stock],
            minimumStock = this[ProductsTable.minimumStock],
            price = this[ProductsTable.price].toPlainString(),
            imageUrl = this[ProductsTable.imageUrl],
            isActive = this[ProductsTable.isActive]
        )
    }
}