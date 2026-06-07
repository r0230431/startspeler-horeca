package com.startspeler.horeca.screens.crew.management.products

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.CatalogApi
import com.startspeler.horeca.data.api.ProductApi
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.products.CreateProductRequest
import com.startspeler.horeca.data.models.products.ProductImageUploadResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.data.models.products.UpdateProductRequest

class ProductViewModel(
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
            categories = catalogApi.getCategories().sortedWith(compareBy(CategoryResponse::displayOrder, CategoryResponse::name))
            products = productApi.getProducts().sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Producten konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun createProduct(request: CreateProductRequest): ApiResult<ProductResponse> {
        val result = productApi.createProduct(request)
        if (result is ApiResult.Success) {
            loadData()
        }
        return result
    }

    suspend fun updateProduct(id: Int, request: UpdateProductRequest): ApiResult<ProductResponse> {
        val result = productApi.updateProduct(id, request)
        if (result is ApiResult.Success) {
            loadData()
        }
        return result
    }

    suspend fun deleteProduct(id: Int): ApiResult<Unit> {
        val result = productApi.deleteProduct(id)
        if (result is ApiResult.Success) {
            loadData()
        }
        return result
    }

    suspend fun uploadImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): ApiResult<ProductImageUploadResponse> {
        return productApi.uploadImage(fileName, mimeType, bytes)
    }

    fun clearError() {
        errorMessage = null
    }
}
