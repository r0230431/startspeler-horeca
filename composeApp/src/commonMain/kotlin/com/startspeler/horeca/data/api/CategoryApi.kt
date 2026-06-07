package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.categories.CreateCategoryRequest
import com.startspeler.horeca.data.models.categories.UpdateCategoryRequest
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.isSuccess

class CategoryApi {
    suspend fun getCategories(): List<CategoryResponse> {
        return HttpClientProvider.client.get("/categories").body()
    }

    suspend fun createCategory(request: CreateCategoryRequest): ApiResult<CategoryResponse> {
        return try {
            val response = HttpClientProvider.client.post("/categories") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Categorie kon niet worden aangemaakt.",
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij aanmaken van categorie.")
        }
    }

    suspend fun updateCategory(id: Int, request: UpdateCategoryRequest): ApiResult<CategoryResponse> {
        return try {
            val response = HttpClientProvider.client.put("/categories/$id") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Categorie kon niet worden bijgewerkt.",
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van categorie.")
        }
    }

    suspend fun deleteCategory(id: Int): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/categories/$id")
            if (response.status.isSuccess()) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Categorie kon niet worden verwijderd.",
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van categorie.")
        }
    }
}
