package com.startspeler.horeca.service

import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.customer.CreateCustomerRequest
import com.startspeler.horeca.dto.customer.CustomerResponse
import com.startspeler.horeca.dto.customer.UpdateCustomerRequest
import com.startspeler.horeca.repository.CustomerRepository

class CustomerService(
    private val customerRepository: CustomerRepository
) {
    fun getAll(): List<CustomerResponse> =
        customerRepository.findAll()

    fun getById(id: Int): CustomerResponse? =
        customerRepository.findById(id)

    fun searchByName(query: String): List<CustomerResponse> =
        customerRepository.searchByName(query)

    fun create(request: CreateCustomerRequest): ServiceResult<CustomerResponse> {
        val trimmedName = request.username.trim()

        if (trimmedName.isBlank()) {
            return ServiceResult.Error("Klantnaam is verplicht.")
        }

        if (trimmedName.length > 50) {
            return ServiceResult.Error("Naam mag maximaal 50 tekens bevatten.")
        }

        val existing = customerRepository.findByName(trimmedName)
        if (existing != null) {
            return ServiceResult.Success(existing)
        }

        val created = customerRepository.create(trimmedName)
        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateCustomerRequest): ServiceResult<CustomerResponse> {
        val existingCustomer = customerRepository.findById(id)
            ?: return ServiceResult.Error("Klant niet gevonden.")

        val trimmedName = request.username.trim()

        if (trimmedName.isBlank()) {
            return ServiceResult.Error("Klantnaam is verplicht.")
        }

        val sameNameCustomer = customerRepository.findByName(trimmedName)
        if (sameNameCustomer != null && sameNameCustomer.id != id) {
            return ServiceResult.Error("Er bestaat al een klant met deze naam.")
        }

        customerRepository.update(id, trimmedName)

        val updated = customerRepository.findById(id) ?: existingCustomer
        return ServiceResult.Success(updated)
    }

    fun delete(id: Int): ServiceResult<Unit> {
        val existingCustomer = customerRepository.findById(id)
            ?: return ServiceResult.Error("Klant niet gevonden.")

        customerRepository.clearCustomerReferences(existingCustomer.id)
        customerRepository.delete(existingCustomer.id)
        return ServiceResult.Success(Unit)
    }

    fun deleteAll(): ServiceResult<Unit> {
        customerRepository.clearAllCustomerReferences()
        customerRepository.deleteAllAndResetIds()
        return ServiceResult.Success(Unit)
    }
}