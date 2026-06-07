package com.startspeler.horeca.screens.crew.customers

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.CustomerApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.customers.CustomerResponse

class CustomerViewModel(
    private val customerApi: CustomerApi = CustomerApi(),
) {
    var customers by mutableStateOf<List<CustomerResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadCustomers() {
        isLoading = true
        errorMessage = null
        try {
            customers = customerApi.getCustomers().sortedBy { it.username.lowercase() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Klanten konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun deleteCustomer(id: Int): ApiResult<Unit> {
        val result = customerApi.deleteCustomer(id)
        if (result is ApiResult.Success) {
            loadCustomers()
        }
        return result
    }

    suspend fun deleteAllCustomers(): ApiResult<Unit> {
        val result = customerApi.deleteAllCustomers()
        if (result is ApiResult.Success) {
            loadCustomers()
        }
        return result
    }

    fun clearError() {
        errorMessage = null
    }
}
