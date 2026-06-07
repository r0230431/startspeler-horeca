package com.startspeler.horeca.routes

import com.startspeler.horeca.database.enums.OrderSource
import com.startspeler.horeca.database.enums.OrderStatus
import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.order.CreateOrderRequest
import com.startspeler.horeca.service.OrderService
import com.startspeler.horeca.dto.order.UpdateOrderRequest
import com.startspeler.horeca.dto.order.UpdateOrderStatusRequest
import io.ktor.server.routing.put
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.patch
import io.ktor.server.routing.route

fun Route.orderRoutes(orderService: OrderService) {
    route("/orders") {
        post {
            val request = call.receive<CreateOrderRequest>()

            when (val result = orderService.create(request)) {
                is ServiceResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                is ServiceResult.Error -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(result.message)
                )
            }
        }

        authenticate("auth-jwt") {

            get {
                val statusParam = call.request.queryParameters["status"]
                val onlyUnpaidParam = call.request.queryParameters["onlyUnpaid"]
                val tableIdParam = call.request.queryParameters["tableId"]
                val customerIdParam = call.request.queryParameters["customerId"]
                val orderSourceParam = call.request.queryParameters["orderSource"]
                val orderedByName = call.request.queryParameters["orderedByName"]?.trim()

                val status = try {
                    statusParam?.let { OrderStatus.valueOf(it) }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldige statusfilter."))
                    return@get
                }

                val onlyUnpaid = onlyUnpaidParam?.toBooleanStrictOrNull()
                if (onlyUnpaidParam != null && onlyUnpaid == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldige onlyUnpaid-filter. Gebruik true of false."))
                    return@get
                }

                val tableId = tableIdParam?.toIntOrNull()
                if (tableIdParam != null && tableId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldige tableId-filter."))
                    return@get
                }

                val customerId = customerIdParam?.toIntOrNull()
                if (customerIdParam != null && customerId == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldige customerId-filter."))
                    return@get
                }

                val orderSource = try {
                    orderSourceParam?.let { OrderSource.valueOf(it) }
                } catch (e: IllegalArgumentException) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldige orderSource-filter."))
                    return@get
                }

                val orders = orderService.getAllFiltered(
                    status = status,
                    onlyUnpaid = onlyUnpaid,
                    tableId = tableId,
                    customerId = customerId,
                    orderSource = orderSource,
                    orderedByName = orderedByName
                )

                call.respond(HttpStatusCode.OK, orders)
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig order-id."))
                    return@get
                }

                val order = orderService.getById(id)
                if (order == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Bestelling niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, order)
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig order-id."))
                    return@put
                }

                val request = call.receive<UpdateOrderRequest>()

                when (val result = orderService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status = when (result.message) {
                            "Bestelling niet gevonden." -> HttpStatusCode.NotFound
                            "Een betaalde bestelling kan niet meer gewijzigd worden." -> HttpStatusCode.BadRequest
                            "Een geleverde bestelling kan niet meer gewijzigd worden." -> HttpStatusCode.BadRequest
                            "Een geannuleerde bestelling kan niet meer gewijzigd worden." -> HttpStatusCode.BadRequest
                            else -> HttpStatusCode.BadRequest
                        }

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            patch("/{id}/status") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig order-id."))
                    return@patch
                }

                val request = call.receive<UpdateOrderStatusRequest>()

                when (val result = orderService.updateStatus(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status = when {
                            result.message == "Bestelling niet gevonden." -> HttpStatusCode.NotFound
                            result.message == "Een betaalde bestelling kan niet meer geannuleerd worden." -> HttpStatusCode.BadRequest
                            result.message.startsWith("Ongeldige statusovergang") -> HttpStatusCode.BadRequest
                            else -> HttpStatusCode.BadRequest
                        }

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }
        }

    }
}