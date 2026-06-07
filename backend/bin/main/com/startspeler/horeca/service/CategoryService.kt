package com.startspeler.horeca.service

import com.startspeler.horeca.dto.category.CategoryResponse
import com.startspeler.horeca.dto.category.CreateCategoryRequest
import com.startspeler.horeca.dto.category.UpdateCategoryRequest
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.repository.CategoryRepository

class CategoryService(
    private val categoryRepository: CategoryRepository,
) {
    fun getAll(): List<CategoryResponse> =
        categoryRepository.findAll()

    fun getById(id: Int): CategoryResponse? =
        categoryRepository.findById(id)

    fun create(request: CreateCategoryRequest): ServiceResult<CategoryResponse> {
        val trimmedName = request.name.trim()
        val trimmedDescription = request.description?.trim()?.ifBlank { null }

        when {
            trimmedName.isBlank() -> return ServiceResult.Error("Categorienaam is verplicht.")
            trimmedName.length > 100 -> return ServiceResult.Error("Categorienaam mag maximaal 100 tekens bevatten.")
            trimmedDescription != null && trimmedDescription.length > 255 -> return ServiceResult.Error("Omschrijving mag maximaal 255 tekens bevatten.")
            request.displayOrder < 0 -> return ServiceResult.Error("Display order mag niet negatief zijn.")
        }

        val existingByName = categoryRepository.findByName(trimmedName)
        if (existingByName != null) {
            return ServiceResult.Error("Er bestaat al een categorie met deze naam.")
        }

        val existingByDisplayOrder = categoryRepository.findByDisplayOrder(request.displayOrder)
        if (existingByDisplayOrder != null) {
            return ServiceResult.Error("Er bestaat al een categorie met deze display order.")
        }

        val created = categoryRepository.create(
            name = trimmedName,
            description = trimmedDescription,
            displayOrder = request.displayOrder,
            isActive = true,
        )

        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateCategoryRequest): ServiceResult<CategoryResponse> {
        val existingCategory = categoryRepository.findById(id)
            ?: return ServiceResult.Error("Categorie niet gevonden.")

        val trimmedName = request.name.trim()
        val trimmedDescription = request.description?.trim()?.ifBlank { null }

        when {
            trimmedName.isBlank() -> return ServiceResult.Error("Categorienaam is verplicht.")
            trimmedName.length > 100 -> return ServiceResult.Error("Categorienaam mag maximaal 100 tekens bevatten.")
            trimmedDescription != null && trimmedDescription.length > 255 -> return ServiceResult.Error("Omschrijving mag maximaal 255 tekens bevatten.")
            request.displayOrder < 0 -> return ServiceResult.Error("Display order mag niet negatief zijn.")
        }

        val categoryWithSameName = categoryRepository.findByName(trimmedName)
        if (categoryWithSameName != null && categoryWithSameName.id != id) {
            return ServiceResult.Error("Er bestaat al een categorie met deze naam.")
        }

        val categoryWithSameDisplayOrder = categoryRepository.findByDisplayOrder(request.displayOrder)
        if (categoryWithSameDisplayOrder != null && categoryWithSameDisplayOrder.id != id) {
            return ServiceResult.Error("Er bestaat al een categorie met deze display order.")
        }

        categoryRepository.update(
            id = id,
            name = trimmedName,
            description = trimmedDescription,
            displayOrder = request.displayOrder,
            isActive = request.isActive,
        )

        val updated = categoryRepository.findById(id)
            ?: existingCategory

        return ServiceResult.Success(updated)
    }

    fun delete(id: Int): ServiceResult<Unit> {
        val existingCategory = categoryRepository.findById(id)
            ?: return ServiceResult.Error("Categorie niet gevonden.")

        if (categoryRepository.hasProducts(existingCategory.id)) {
            return ServiceResult.Error(
                "Deze categorie kan niet verwijderd worden omdat er nog producten aan gekoppeld zijn. Zet de categorie eerst op inactief of koppel de producten aan een andere categorie."
            )
        }

        val deleted = categoryRepository.deleteSafely(existingCategory.id)
        if (!deleted) {
            return ServiceResult.Error("Categorie kon niet verwijderd worden.")
        }

        return ServiceResult.Success(Unit)
    }
}
