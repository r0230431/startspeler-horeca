package com.startspeler.horeca.config

import com.startspeler.horeca.database.tables.CafeTablesTable
import com.startspeler.horeca.database.tables.CategoriesTable
import com.startspeler.horeca.database.tables.CrewMembersTable
import com.startspeler.horeca.database.tables.CustomersTable
import com.startspeler.horeca.database.tables.DiscountsTable
import com.startspeler.horeca.database.tables.OrderLinesTable
import com.startspeler.horeca.database.tables.OrdersTable
import com.startspeler.horeca.database.tables.PaymentDiscountsTable
import com.startspeler.horeca.database.tables.PaymentsTable
import com.startspeler.horeca.database.tables.ProductsTable
import com.zaxxer.hikari.HikariConfig
import com.zaxxer.hikari.HikariDataSource
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {

    fun init() {
        Database.connect(createDataSource())

        // Generate tables if they do not already exist in the database
        transaction {
            SchemaUtils.create(
                CrewMembersTable,
                CustomersTable,
                CafeTablesTable,
                CategoriesTable,
                ProductsTable,
                DiscountsTable,
                PaymentsTable,
                PaymentDiscountsTable,
                OrdersTable,
                OrderLinesTable
            )
        }
    }

    private fun createDataSource(): DataSource {
        val host = System.getenv("DB_HOST") ?: "localhost"
        val port = System.getenv("DB_PORT") ?: "3306"
        val dbName = System.getenv("DB_NAME") ?: "startspeler_horeca"

        val config = HikariConfig().apply {
            driverClassName = System.getenv("DB_DRIVER") ?: "com.mysql.cj.jdbc.Driver"
            jdbcUrl = System.getenv("DB_URL")
                ?: "jdbc:mysql://$host:$port/$dbName?useSSL=false&allowPublicKeyRetrieval=true&preserveInstants=false"
            username = System.getenv("DB_USER") ?: "root"
            password = System.getenv("DB_PASSWORD") ?: ""

            maximumPoolSize = (System.getenv("DB_MAX_POOL_SIZE") ?: "10").toInt()
            minimumIdle = (System.getenv("DB_MIN_IDLE") ?: "2").toInt()
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            poolName = "StartSpelerPool"
            connectionTimeout = 10000
            idleTimeout = 60000
            maxLifetime = 1800000

            validate()
        }

        return HikariDataSource(config)
    }

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
}