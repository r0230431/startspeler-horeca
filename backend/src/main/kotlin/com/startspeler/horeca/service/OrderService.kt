package com.startspeler.horeca.service

import com.startspeler.horeca.database.enums.OrderSource
import com.startspeler.horeca.dto.order.CreateOrderRequest
import com.startspeler.horeca.dto.order.OrderResponse
import com.startspeler.horeca.repository.CafeTableRepository
import com.startspeler.horeca.repository.CustomerRepository
import com.startspeler.horeca.repository.OrderRepository
import com.startspeler.horeca.repository.ProductRepository
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.database.enums.OrderStatus
import com.startspeler.horeca.dto.order.UpdateOrderRequest
import com.startspeler.horeca.dto.order.UpdateOrderStatusRequest

class OrderService(
    private val orderRepository: OrderRepository,
    private val customerRepository: CustomerRepository,
    private val cafeTableRepository: CafeTableRepository,
    private val productRepository: ProductRepository
) {
    fun getAll(): List<OrderResponse> =
        orderRepository.findAll()

    fun getById(id: Int): OrderResponse? =
        orderRepository.findById(id)

    fun getAllFiltered(
        status: OrderStatus? = null,
        onlyUnpaid: Boolean? = null,
        tableId: Int? = null,
        customerId: Int? = null,
        orderSource: OrderSource? = null,
        orderedByName: String? = null
    ): List<OrderResponse> =
        orderRepository.findAllFiltered(
            status = status,
            onlyUnpaid = onlyUnpaid,
            tableId = tableId,
            customerId = customerId,
            orderSource = orderSource,
            orderedByName = orderedByName
        )

    fun create(request: CreateOrderRequest): ServiceResult<OrderResponse> {
        val orderedByName = request.orderedByName.trim()

        if (orderedByName.isBlank()) {
            return ServiceResult.Error("Naam van de besteller is verplicht.")
        }

        if (orderedByName.length > 50) {
            return ServiceResult.Error("Naam mag maximaal 50 tekens bevatten.")
        }

        val trimmedNote = request.note?.trim()
        if (trimmedNote != null && trimmedNote.length > 300) {
            return ServiceResult.Error("Opmerking mag maximaal 300 tekens bevatten.")
        }

        if (request.lines.isEmpty()) {
            return ServiceResult.Error("Een bestelling moet minstens één product bevatten.")
        }

        val table = cafeTableRepository.findById(request.tableId)
            ?: return ServiceResult.Error("Tafel niet gevonden.")

        if (request.customerId != null) {
            val customer = customerRepository.findById(request.customerId)
                ?: return ServiceResult.Error("Klant niet gevonden.")
        }

        val lineData = mutableListOf<OrderRepository.CreateOrderLineData>()

        for (line in request.lines) {
            if (line.quantity < 1) {
                return ServiceResult.Error("Aantal moet minstens 1 zijn.")
            }

            val product = productRepository.findById(line.productId)
                ?: return ServiceResult.Error("Product met id ${line.productId} niet gevonden.")

            if (!product.isActive) {
                return ServiceResult.Error("Product '${product.name}' is niet actief.")
            }

            if (product.stock <= 0) {
                return ServiceResult.Error("Product '${product.name}' is uitverkocht.")
            }

            if (product.stock < line.quantity) {
                return ServiceResult.Error("Onvoldoende voorraad voor product '${product.name}'.")
            }

            lineData += OrderRepository.CreateOrderLineData(
                productId = product.id,
                productNameSnapshot = product.name,
                unitPriceSnapshot = product.price.toBigDecimal(),
                quantity = line.quantity
            )
        }

        // Verlaag stock atomisch vóór het aanmaken van de order (voorkomt race conditions)
        val decreasedSoFar = mutableListOf<Pair<Int, Int>>() // productId to quantity
        for (line in lineData) {
            val success = productRepository.decreaseStock(line.productId, line.quantity)
            if (!success) {
                // Herstel reeds verlaagde stocks
                for ((productId, qty) in decreasedSoFar) {
                    productRepository.restoreStock(productId, qty)
                }
                return ServiceResult.Error("Onvoldoende voorraad voor product '${line.productNameSnapshot}'. Iemand anders bestelde net het laatste exemplaar.")
            }
            decreasedSoFar += line.productId to line.quantity
        }

        val created = orderRepository.createOrder(
            customerId = request.customerId,
            orderedByName = orderedByName,
            tableId = table.id,
            note = request.note?.trim()?.takeIf { it.isNotBlank() },
            orderSource = request.orderSource,
            lines = lineData
        )

        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateOrderRequest): ServiceResult<OrderResponse> {
        val existingOrder = orderRepository.findById(id)
            ?: return ServiceResult.Error("Bestelling niet gevonden.")

        if (existingOrder.paymentId != null) {
            return ServiceResult.Error("Een betaalde bestelling kan niet meer gewijzigd worden.")
        }

        if (existingOrder.status == OrderStatus.DELIVERED) {
            return ServiceResult.Error("Een geleverde bestelling kan niet meer gewijzigd worden.")
        }

        if (existingOrder.status == OrderStatus.CANCELLED) {
            return ServiceResult.Error("Een geannuleerde bestelling kan niet meer gewijzigd worden.")
        }

        val orderedByName = request.orderedByName.trim()

        if (orderedByName.isBlank()) {
            return ServiceResult.Error("Naam van de besteller is verplicht.")
        }

        if (request.lines.isEmpty()) {
            return ServiceResult.Error("Een bestelling moet minstens één product bevatten.")
        }

        val table = cafeTableRepository.findById(request.tableId)
            ?: return ServiceResult.Error("Tafel niet gevonden.")

        if (request.customerId != null) {
            val customer = customerRepository.findById(request.customerId)
                ?: return ServiceResult.Error("Klant niet gevonden.")
        }

        val lineData = mutableListOf<OrderRepository.CreateOrderLineData>()

        for (line in request.lines) {
            if (line.quantity < 1) {
                return ServiceResult.Error("Aantal moet minstens 1 zijn.")
            }

            val product = productRepository.findById(line.productId)
                ?: return ServiceResult.Error("Product met id ${line.productId} niet gevonden.")

            if (!product.isActive) {
                return ServiceResult.Error("Product '${product.name}' is niet actief.")
            }

            if (product.stock <= 0) {
                return ServiceResult.Error("Product '${product.name}' is uitverkocht.")
            }

            if (product.stock < line.quantity) {
                return ServiceResult.Error("Onvoldoende voorraad voor product '${product.name}'.")
            }

            val unitPrice = product.price.toBigDecimal()

            lineData += OrderRepository.CreateOrderLineData(
                productId = product.id,
                productNameSnapshot = product.name,
                unitPriceSnapshot = unitPrice,
                quantity = line.quantity
            )
        }

        val updated = orderRepository.updateOrder(
            id = id,
            customerId = request.customerId,
            orderedByName = orderedByName,
            tableId = table.id,
            note = request.note?.trim()?.takeIf { it.isNotBlank() },
            orderSource = request.orderSource,
            lines = lineData
        ) ?: return ServiceResult.Error("Bestelling niet gevonden.")

        return ServiceResult.Success(updated)
    }

    fun updateStatus(id: Int, request: UpdateOrderStatusRequest): ServiceResult<OrderResponse> {
        val existingOrder = orderRepository.findById(id)
            ?: return ServiceResult.Error("Bestelling niet gevonden.")

        val currentStatus = existingOrder.status
        val newStatus = request.status

        val isValidTransition = when (currentStatus) {
            OrderStatus.IN_PROGRESS -> newStatus == OrderStatus.READY || newStatus == OrderStatus.CANCELLED
            OrderStatus.READY -> newStatus == OrderStatus.DELIVERED || newStatus == OrderStatus.CANCELLED
            OrderStatus.DELIVERED -> false
            OrderStatus.CANCELLED -> false
        }

        if (!isValidTransition) {
            return ServiceResult.Error("Ongeldige statusovergang van $currentStatus naar $newStatus.")
        }

        if (existingOrder.paymentId != null && newStatus == OrderStatus.CANCELLED) {
            return ServiceResult.Error("Een betaalde bestelling kan niet meer geannuleerd worden.")
        }

        val success = orderRepository.updateStatusAndDecreaseStockIfNeeded(id, newStatus)
        if (!success) {
            return ServiceResult.Error("Status aanpassen mislukt.")
        }

        return ServiceResult.Success(orderRepository.findById(id)!!)
    }
}