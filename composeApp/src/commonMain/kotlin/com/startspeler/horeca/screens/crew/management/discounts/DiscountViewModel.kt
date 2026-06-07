package com.startspeler.horeca.screens.crew.discounts

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.DiscountApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.discounts.CreateDiscountRequest
import com.startspeler.horeca.data.models.discounts.DiscountResponse
import com.startspeler.horeca.data.models.discounts.UpdateDiscountRequest

class DiscountViewModel(
    private val discountApi: DiscountApi = DiscountApi(),
) {
    var discounts by mutableStateOf<List<DiscountResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadDiscounts() {
        isLoading = true
        errorMessage = null
        try {
            discounts = discountApi.getDiscounts().sortedBy { it.name.lowercase() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Kortingen konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun createDiscount(request: CreateDiscountRequest): ApiResult<DiscountResponse> {
        val result = discountApi.createDiscount(request)
        if (result is ApiResult.Success) {
            loadDiscounts()
        }
        return result
    }

    suspend fun updateDiscount(id: Int, request: UpdateDiscountRequest): ApiResult<DiscountResponse> {
        val result = discountApi.updateDiscount(id, request)
        if (result is ApiResult.Success) {
            loadDiscounts()
        }
        return result
    }

    suspend fun deleteDiscount(id: Int): ApiResult<Unit> {
        val result = discountApi.deleteDiscount(id)
        if (result is ApiResult.Success) {
            loadDiscounts()
        }
        return result
    }

    fun clearError() {
        errorMessage = null
    }
}
