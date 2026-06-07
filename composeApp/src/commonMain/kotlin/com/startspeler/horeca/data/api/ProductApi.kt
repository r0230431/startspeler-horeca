package com.startspeler.horeca.data.api

import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.products.AddProductDeliveryRequest
import com.startspeler.horeca.data.models.products.CreateProductRequest
import com.startspeler.horeca.data.models.products.ProductImageUploadResponse
import com.startspeler.horeca.data.models.products.ProductResponse
import com.startspeler.horeca.data.models.products.UpdateProductInventoryRequest
import com.startspeler.horeca.data.models.products.UpdateProductRequest
import com.startspeler.horeca.data.models.products.UpdateProductStockRequest
import com.startspeler.horeca.network.HttpClientProvider
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.forms.formData
import io.ktor.client.request.forms.submitFormWithBinaryData
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.Headers
import io.ktor.http.HttpHeaders
import io.ktor.http.isSuccess

class ProductApi {
    suspend fun getProducts(): List<ProductResponse> {
        return HttpClientProvider.client.get("/products").body()
    }

    suspend fun getProductById(id: Int): ProductResponse {
        return HttpClientProvider.client.get("/products/$id").body()
    }

    suspend fun createProduct(request: CreateProductRequest): ApiResult<ProductResponse> {
        return try {
            val response = HttpClientProvider.client.post("/products") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Product kon niet worden aangemaakt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij aanmaken van product.")
        }
    }

    suspend fun updateProduct(id: Int, request: UpdateProductRequest): ApiResult<ProductResponse> {
        return try {
            val response = HttpClientProvider.client.put("/products/$id") {
                setBody(request)
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Product kon niet worden bijgewerkt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van product.")
        }
    }

    suspend fun updateStock(id: Int, stock: Int): ApiResult<ProductResponse> {
        return try {
            val response = HttpClientProvider.client.put("/products/$id/stock") {
                setBody(UpdateProductStockRequest(stock = stock))
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Voorraad kon niet worden bijgewerkt."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij bijwerken van voorraad.")
        }
    }

    suspend fun updateInventory(id: Int, stock: Int, minimumStock: Int): ApiResult<ProductResponse> {
        return try {
            val response = HttpClientProvider.client.put("/products/$id/inventory") {
                setBody(
                    UpdateProductInventoryRequest(
                        stock = stock,
                        minimumStock = minimumStock,
                    )
                )
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Voorraad kon niet worden opgeslagen."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij opslaan van voorraad.")
        }
    }

    suspend fun addDelivery(id: Int, quantity: Int): ApiResult<ProductResponse> {
        return try {
            val response = HttpClientProvider.client.post("/products/$id/deliveries") {
                setBody(AddProductDeliveryRequest(quantity = quantity))
            }

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Levering kon niet worden toegevoegd."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij toevoegen van levering.")
        }
    }

    suspend fun deleteProduct(id: Int): ApiResult<Unit> {
        return try {
            val response = HttpClientProvider.client.delete("/products/$id")
            if (response.status.isSuccess()) {
                ApiResult.Success(Unit)
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Product kon niet worden verwijderd."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij verwijderen van product.")
        }
    }

    suspend fun uploadImage(
        fileName: String,
        mimeType: String,
        bytes: ByteArray,
    ): ApiResult<ProductImageUploadResponse> {
        return try {
            val response = HttpClientProvider.client.submitFormWithBinaryData(
                url = "/products/upload-image",
                formData = formData {
                    append(
                        key = "file",
                        value = bytes,
                        headers = Headers.build {
                            append(HttpHeaders.ContentDisposition, "filename=\"$fileName\"")
                            append(HttpHeaders.ContentType, mimeType)
                        }
                    )
                }
            )

            if (response.status.isSuccess()) {
                ApiResult.Success(response.body())
            } else {
                ApiResult.Error(
                    extractApiMessage(
                        response.bodyAsText(),
                        "Afbeelding kon niet worden opgeladen."
                    )
                )
            }
        } catch (e: Exception) {
            ApiResult.Error(e.message ?: "Onbekende fout bij opladen van afbeelding.")
        }
    }
}
