package com.startspeler.horeca.customer

import com.startspeler.horeca.data.models.customers.CustomerResponse
import com.startspeler.horeca.data.models.tables.TableResponse

data class CustomerSession(
    val customer: CustomerResponse,
    val table: TableResponse,
) {
    val displayName: String get() = customer.username
    val tableLabel: String get() = table.tableNumber.toString()
}
