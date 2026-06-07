package com.startspeler.horeca.service

import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.product.CreateProductRequest
import com.startspeler.horeca.dto.product.ProductImageUploadResponse
import com.startspeler.horeca.dto.product.ProductResponse
import com.startspeler.horeca.dto.product.UpdateProductRequest
import com.startspeler.horeca.repository.ProductRepository
import java.math.BigDecimal

class ProductService(
    private val productRepository: ProductRepository,
    private val productImageStorage: ProductImageStorage = ProductImageStorage(),
) {
    fun getAll(): List<ProductResponse> =
        productRepository.findAll()

    fun getAllActive(): List<ProductResponse> =
        productRepository.findAllActive()

    fun getById(id: Int): ProductResponse? =
        productRepository.findById(id)

    fun uploadImage(
        bytes: ByteArray,
        originalFileName: String?,
        contentType: String?,
    ): ServiceResult<ProductImageUploadResponse> {
        return when (val result = productImageStorage.storeImage(bytes, originalFileName, contentType)) {
            is ServiceResult.Success -> ServiceResult.Success(
                ProductImageUploadResponse(
                    imageUrl = result.data,
                    originalFileName = originalFileName?.trim().orEmpty()
                )
            )

            is ServiceResult.Error -> ServiceResult.Error(result.message)
        }
    }

    fun create(request: CreateProductRequest): ServiceResult<ProductResponse> {
        validateProductInput(
            categoryId = request.categoryId,
            name = request.name,
            description = request.description,
            stock = request.stock,
            minimumStock = request.minimumStock,
            price = request.price,
            currentProductId = null
        )?.let { return it }

        val created = productRepository.create(
            categoryId = request.categoryId,
            name = request.name.trim(),
            description = request.description?.trim()?.ifBlank { null },
            stock = request.stock,
            minimumStock = request.minimumStock,
            price = request.price.trim().replace(',', '.').toBigDecimal(),
            imageUrl = request.imageUrl?.trim()?.ifBlank { null },
            isActive = true
        )

        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateProductRequest): ServiceResult<ProductResponse> {
        val existingProduct = productRepository.findById(id)
            ?: return ServiceResult.Error("Product niet gevonden.")

        validateProductInput(
            categoryId = request.categoryId,
            name = request.name,
            description = request.description,
            stock = request.stock,
            minimumStock = request.minimumStock,
            price = request.price,
            currentProductId = id
        )?.let { return it }

        val newImageUrl = request.imageUrl?.trim()?.ifBlank { null }

        productRepository.update(
            id = id,
            categoryId = request.categoryId,
            name = request.name.trim(),
            description = request.description?.trim()?.ifBlank { null },
            stock = request.stock,
            minimumStock = request.minimumStock,
            price = request.price.trim().replace(',', '.').toBigDecimal(),
            imageUrl = newImageUrl,
            isActive = request.isActive
        )

        if (existingProduct.imageUrl != null && existingProduct.imageUrl != newImageUrl) {
            productImageStorage.deleteByPublicPath(existingProduct.imageUrl)
        }

        val updated = productRepository.findById(id) ?: existingProduct
        return ServiceResult.Success(updated)
    }

    fun updateStock(id: Int, stock: Int): ServiceResult<ProductResponse> {
        val existingProduct = productRepository.findById(id)
            ?: return ServiceResult.Error("Product niet gevonden.")

        if (stock < 0) {
            return ServiceResult.Error("Huidige voorraad mag niet negatief zijn.")
        }

        productRepository.updateStock(id, stock)
        return ServiceResult.Success(productRepository.findById(id) ?: existingProduct)
    }

    fun updateInventory(id: Int, stock: Int, minimumStock: Int): ServiceResult<ProductResponse> {
        val existingProduct = productRepository.findById(id)
            ?: return ServiceResult.Error("Product niet gevonden.")

        if (stock < 0) {
            return ServiceResult.Error("Huidige voorraad mag niet negatief zijn.")
        }

        if (minimumStock < 0) {
            return ServiceResult.Error("Minimumvoorraad mag niet negatief zijn.")
        }

        productRepository.updateInventory(id, stock, minimumStock)
        return ServiceResult.Success(productRepository.findById(id) ?: existingProduct)
    }

    fun addDelivery(id: Int, quantity: Int): ServiceResult<ProductResponse> {
        productRepository.findById(id)
            ?: return ServiceResult.Error("Product niet gevonden.")

        if (quantity <= 0) {
            return ServiceResult.Error("Levering moet groter zijn dan 0.")
        }

        val updated = productRepository.addDelivery(id, quantity)
            ?: return ServiceResult.Error("Levering kon niet worden verwerkt.")

        return ServiceResult.Success(updated)
    }

    fun delete(id: Int): ServiceResult<Unit> {
        val existingProduct = productRepository.findById(id)
            ?: return ServiceResult.Error("Product niet gevonden.")

        val deleted = productRepository.deleteSafely(existingProduct.id)
        if (!deleted) {
            return ServiceResult.Error("Product kan niet verwijderd worden omdat het nog in bestellingen voorkomt.")
        }

        productImageStorage.deleteByPublicPath(existingProduct.imageUrl)
        return ServiceResult.Success(Unit)
    }

    private fun validateProductInput(
        categoryId: Int,
        name: String,
        description: String?,
        stock: Int,
        minimumStock: Int,
        price: String,
        currentProductId: Int?,
    ): ServiceResult.Error? {
        val trimmedName = name.trim()
        if (trimmedName.isBlank()) {
            return ServiceResult.Error("Productnaam is verplicht.")
        }

        if (trimmedName.length < 2) {
            return ServiceResult.Error("Productnaam moet minstens 2 tekens bevatten.")
        }

        if (trimmedName.length > 150) {
            return ServiceResult.Error("Productnaam is te lang.")
        }

        if ((description?.length ?: 0) > 255) {
            return ServiceResult.Error("Omschrijving mag maximaal 255 tekens bevatten.")
        }

        if (stock < 0) {
            return ServiceResult.Error("Huidige voorraad mag niet negatief zijn.")
        }

        if (minimumStock < 0) {
            return ServiceResult.Error("Minimumvoorraad mag niet negatief zijn.")
        }

        val normalizedPrice = price.trim().replace(',', '.')
        val priceRegex = Regex("""^\d+(\.\d{1,2})?$""")
        if (!priceRegex.matches(normalizedPrice)) {
            return ServiceResult.Error("Prijs moet een geldig bedrag zijn met maximaal 2 decimalen.")
        }

        val parsedPrice = normalizedPrice.toBigDecimalOrNull()
            ?: return ServiceResult.Error("Prijs heeft geen geldig formaat.")

        if (parsedPrice < BigDecimal.ZERO) {
            return ServiceResult.Error("Prijs mag niet negatief zijn.")
        }

        if (!productRepository.categoryExists(categoryId)) {
            return ServiceResult.Error("Categorie niet gevonden.")
        }

        val duplicate = productRepository.findAll()
            .firstOrNull {
                it.name.equals(trimmedName, ignoreCase = true) && it.id != currentProductId
            }

        if (duplicate != null) {
            return ServiceResult.Error("Er bestaat al een product met deze naam.")
        }

        return null
    }
}