package com.startspeler.horeca.screens.crew.management.categories

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.CategoryApi
import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.categories.CreateCategoryRequest
import com.startspeler.horeca.data.models.categories.UpdateCategoryRequest
import com.startspeler.horeca.data.models.common.ApiResult

class CategoryViewModel(
    private val categoryApi: CategoryApi = CategoryApi(),
) {
    var categories by mutableStateOf<List<CategoryResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadCategories() {
        isLoading = true
        errorMessage = null
        try {
            categories = categoryApi.getCategories().sortedWith(compareBy(CategoryResponse::displayOrder, CategoryResponse::name))
        } catch (e: Exception) {
            errorMessage = e.message ?: "Categorieën konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun createCategory(request: CreateCategoryRequest): ApiResult<CategoryResponse> {
        val result = categoryApi.createCategory(request)
        if (result is ApiResult.Success) {
            loadCategories()
        }
        return result
    }

    suspend fun updateCategory(id: Int, request: UpdateCategoryRequest): ApiResult<CategoryResponse> {
        val result = categoryApi.updateCategory(id, request)
        if (result is ApiResult.Success) {
            loadCategories()
        }
        return result
    }

    suspend fun deleteCategory(id: Int): ApiResult<Unit> {
        val result = categoryApi.deleteCategory(id)
        if (result is ApiResult.Success) {
            loadCategories()
        }
        return result
    }

    fun clearError() {
        errorMessage = null
    }
}
