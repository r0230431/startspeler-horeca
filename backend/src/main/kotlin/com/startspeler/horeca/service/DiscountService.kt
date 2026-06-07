package com.startspeler.horeca.service

import com.startspeler.horeca.database.enums.DiscountType
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.discount.CreateDiscountRequest
import com.startspeler.horeca.dto.discount.DiscountResponse
import com.startspeler.horeca.dto.discount.UpdateDiscountRequest
import com.startspeler.horeca.repository.DiscountRepository
import java.math.BigDecimal

class DiscountService(
    private val discountRepository: DiscountRepository
) {

    fun getAll(): List<DiscountResponse> =
        discountRepository.findAll()

    fun getById(id: Int): DiscountResponse? =
        discountRepository.findById(id)

    fun create(request: CreateDiscountRequest): ServiceResult<DiscountResponse> {
        val trimmedName = request.name.trim()

        if (trimmedName.isBlank()) {
            return ServiceResult.Error("Naam van de korting is verplicht.")
        }

        if (trimmedName.length > 100) {
            return ServiceResult.Error("Naam mag maximaal 100 tekens bevatten.")
        }

        val normalizedDescription = request.description?.trim()?.ifBlank { null }
        if (normalizedDescription != null && normalizedDescription.length > 255) {
            return ServiceResult.Error("Omschrijving mag maximaal 255 tekens bevatten.")
        }

        val parsedValue = parseAndValidateDiscountValue(
            value = request.discountValue,
            discountType = request.discountType
        ) ?: return ServiceResult.Error(getDiscountValueErrorMessage(request.discountType))

        val existing = discountRepository.findByName(trimmedName)
        if (existing != null) {
            return ServiceResult.Error("Er bestaat al een korting met deze naam.")
        }

        val created = discountRepository.create(
            name = trimmedName,
            description = normalizedDescription,
            discountType = request.discountType,
            discountValue = parsedValue,
            isActive = true
        )

        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateDiscountRequest): ServiceResult<DiscountResponse> {
        val existingDiscount = discountRepository.findById(id)
            ?: return ServiceResult.Error("Korting niet gevonden.")

        val trimmedName = request.name.trim()

        if (trimmedName.isBlank()) {
            return ServiceResult.Error("Naam van de korting is verplicht.")
        }

        if (trimmedName.length > 100) {
            return ServiceResult.Error("Naam mag maximaal 100 tekens bevatten.")
        }

        val normalizedDescription = request.description?.trim()?.ifBlank { null }
        if (normalizedDescription != null && normalizedDescription.length > 255) {
            return ServiceResult.Error("Omschrijving mag maximaal 255 tekens bevatten.")
        }

        val parsedValue = parseAndValidateDiscountValue(
            value = request.discountValue,
            discountType = request.discountType
        ) ?: return ServiceResult.Error(getDiscountValueErrorMessage(request.discountType))

        val discountWithSameName = discountRepository.findByName(trimmedName)
        if (discountWithSameName != null && discountWithSameName.id != id) {
            return ServiceResult.Error("Er bestaat al een korting met deze naam.")
        }

        discountRepository.update(
            id = id,
            name = trimmedName,
            description = normalizedDescription,
            discountType = request.discountType,
            discountValue = parsedValue,
            isActive = request.isActive
        )

        val updated = discountRepository.findById(id) ?: existingDiscount
        return ServiceResult.Success(updated)
    }

    fun delete(id: Int): ServiceResult<Unit> {
        val existingDiscount = discountRepository.findById(id)
            ?: return ServiceResult.Error("Korting niet gevonden.")

        discountRepository.delete(existingDiscount.id)
        return ServiceResult.Success(Unit)
    }

    private fun parseAndValidateDiscountValue(
        value: String,
        discountType: DiscountType
    ): BigDecimal? {
        val parsed = value.trim().replace(",", ".").toBigDecimalOrNull() ?: return null

        if (parsed <= BigDecimal.ZERO) return null

        if (discountType == DiscountType.PERCENTAGE && parsed > BigDecimal("100")) {
            return null
        }

        return parsed
    }

    private fun getDiscountValueErrorMessage(discountType: DiscountType): String {
        return when (discountType) {
            DiscountType.PERCENTAGE ->
                "Percentagekorting moet groter zijn dan 0 en mag maximaal 100 zijn."
            DiscountType.FIXED_AMOUNT ->
                "Vast kortingsbedrag moet groter zijn dan 0."
        }
    }
}