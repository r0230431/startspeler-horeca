package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.categories.CategoryResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.get

class CatalogApi {
    suspend fun getCategories(): List<CategoryResponse> {
        return HttpClientProvider.client.get("/categories").body()
    }

    suspend fun getProducts(): List<ProductResponse> {
        return HttpClientProvider.client.get("/products").body()
    }

    suspend fun getPublicProducts(): List<ProductResponse> {
        return HttpClientProvider.client.get("/public/products").body()
    }
}