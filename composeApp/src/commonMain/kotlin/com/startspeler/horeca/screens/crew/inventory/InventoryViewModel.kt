package com.startspeler.horeca.screens.crew.inventory

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.CatalogApi
import com.startspeler.horeca.data.api.ProductApi
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.products.ProductResponse

class InventoryViewModel(
    private val productApi: ProductApi = ProductApi(),
    private val catalogApi: CatalogApi = CatalogApi(),
) {
    var products by mutableStateOf<List<ProductResponse>>(emptyList())
        private set

    var categories by mutableStateOf<List<CategoryResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadData() {
        isLoading = true
        errorMessage = null
        try {
            categories = catalogApi
                .getCategories()
                .filter { it.isActive }
                .sortedWith(compareBy(CategoryResponse::displayOrder, CategoryResponse::name))

            products = productApi
                .getProducts()
                .filter { it.isActive }
                .sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Voorraad kon niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun updateStock(productId: Int, stock: Int): ApiResult<ProductResponse> {
        val result = productApi.updateStock(productId, stock)
        if (result is ApiResult.Success) {
            loadData()
        }
        return result
    }

    suspend fun updateInventory(productId: Int, stock: Int, minimumStock: Int): ApiResult<ProductResponse> {
        val result = productApi.updateInventory(productId, stock, minimumStock)
        if (result is ApiResult.Success) {
            loadData()
        }
        return result
    }

    suspend fun addDelivery(productId: Int, quantity: Int): ApiResult<ProductResponse> {
        val result = productApi.addDelivery(productId, quantity)
        if (result is ApiResult.Success) {
            loadData()
        }
        return result
    }

    fun clearError() {
        errorMessage = null
    }
}
