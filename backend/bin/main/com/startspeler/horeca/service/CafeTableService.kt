package com.startspeler.horeca.service

import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.table.CreateTableRequest
import com.startspeler.horeca.dto.table.TableResponse
import com.startspeler.horeca.dto.table.UpdateTableRequest
import com.startspeler.horeca.repository.CafeTableRepository

class CafeTableService(
    private val cafeTableRepository: CafeTableRepository
) {
    fun getAll(): List<TableResponse> =
        cafeTableRepository.findAll()

    fun getById(id: Int): TableResponse? =
        cafeTableRepository.findById(id)

    fun getByTableNumber(tableNumber: Int): TableResponse? =
        cafeTableRepository.findByTableNumber(tableNumber)

    fun create(request: CreateTableRequest): ServiceResult<TableResponse> {
        if (request.tableNumber <= 0) {
            return ServiceResult.Error("Tafelnummer moet groter zijn dan 0.")
        }

        if (request.seatCount < 0) {
            return ServiceResult.Error("Aantal zitplaatsen mag niet negatief zijn.")
        }

        val existing = cafeTableRepository.findByTableNumber(request.tableNumber)
        if (existing != null) {
            return ServiceResult.Error("Er bestaat al een tafel met dit tafelnummer.")
        }

        val created = cafeTableRepository.create(
            tableNumber = request.tableNumber,
            seatCount = request.seatCount,
            note = request.note?.trim()?.ifBlank { null },
        )

        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateTableRequest): ServiceResult<TableResponse> {
        val existingTable = cafeTableRepository.findById(id)
            ?: return ServiceResult.Error("Tafel niet gevonden.")

        if (request.tableNumber <= 0) {
            return ServiceResult.Error("Tafelnummer moet groter zijn dan 0.")
        }

        if (request.seatCount < 0) {
            return ServiceResult.Error("Aantal zitplaatsen mag niet negatief zijn.")
        }

        val sameNumberTable = cafeTableRepository.findByTableNumber(request.tableNumber)
        if (sameNumberTable != null && sameNumberTable.id != id) {
            return ServiceResult.Error("Er bestaat al een tafel met dit tafelnummer.")
        }

        cafeTableRepository.update(
            id = id,
            tableNumber = request.tableNumber,
            seatCount = request.seatCount,
            note = request.note?.trim()?.ifBlank { null },
        )

        val updated = cafeTableRepository.findById(id) ?: existingTable
        return ServiceResult.Success(updated)
    }

    fun delete(id: Int): ServiceResult<Unit> {
        val existingTable = cafeTableRepository.findById(id)
            ?: return ServiceResult.Error("Tafel niet gevonden.")

        cafeTableRepository.delete(existingTable.id)
        return ServiceResult.Success(Unit)
    }
}