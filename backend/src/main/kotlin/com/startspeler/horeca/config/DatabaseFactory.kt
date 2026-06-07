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
import io.ktor.server.config.ApplicationConfig
import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import javax.sql.DataSource

object DatabaseFactory {

    fun init(appConfig: ApplicationConfig) {
        Database.connect(createDataSource(appConfig))

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

    private fun createDataSource(appConfig: ApplicationConfig): DataSource {
        val host = appConfig.stringOrNull("db.host") ?: "localhost"
        val port = appConfig.stringOrNull("db.port") ?: "3306"
        val dbName = appConfig.stringOrNull("db.name") ?: "startspeler_horeca"

        val hikariConfig = HikariConfig().apply {
            driverClassName = appConfig.stringOrNull("db.driver") ?: "com.mysql.cj.jdbc.Driver"
            jdbcUrl = appConfig.stringOrNull("db.url")
                ?: "jdbc:mysql://$host:$port/$dbName?useSSL=false&allowPublicKeyRetrieval=true&preserveInstants=false"
            username = appConfig.stringOrNull("db.user") ?: "root"
            password = appConfig.stringOrNull("db.password") ?: ""

            maximumPoolSize = appConfig.intOrNull("db.maxPoolSize") ?: 10
            minimumIdle = appConfig.intOrNull("db.minIdle") ?: 2
            isAutoCommit = false
            transactionIsolation = "TRANSACTION_REPEATABLE_READ"

            poolName = "StartSpelerPool"
            connectionTimeout = 10000
            idleTimeout = 60000
            maxLifetime = 1800000

            validate()
        }

        return HikariDataSource(hikariConfig)
    }

    private fun ApplicationConfig.stringOrNull(path: String): String? =
        propertyOrNull(path)?.getString()?.takeIf { it.isNotBlank() }

    private fun ApplicationConfig.intOrNull(path: String): Int? =
        stringOrNull(path)?.toIntOrNull()

    suspend fun <T> dbQuery(block: suspend () -> T): T =
        newSuspendedTransaction(Dispatchers.IO) {
            block()
        }
}