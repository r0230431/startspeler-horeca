package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.MessageResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.customer.CreateCustomerRequest
import com.startspeler.horeca.dto.customer.UpdateCustomerRequest
import com.startspeler.horeca.service.CustomerService
import com.startspeler.horeca.util.requireAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route

fun Route.customerRoutes(customerService: CustomerService) {
    route("/customers") {
        post {
            val request = call.receive<CreateCustomerRequest>()

            when (val result = customerService.create(request)) {
                is ServiceResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                is ServiceResult.Error -> call.respond(
                    HttpStatusCode.BadRequest,
                    ErrorResponse(result.message)
                )
            }
        }

        authenticate("auth-jwt") {
            get {
                call.respond(HttpStatusCode.OK, customerService.getAll())
            }

            delete {
                if (!call.requireAdmin()) return@delete

                when (val result = customerService.deleteAll()) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Alle klanten werden succesvol verwijderd.")
                    )

                    is ServiceResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(result.message)
                    )
                }
            }

            get("/search") {
                val query = call.request.queryParameters["query"].orEmpty()
                call.respond(HttpStatusCode.OK, customerService.searchByName(query))
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig klant-id."))
                    return@get
                }

                val customer = customerService.getById(id)
                if (customer == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Klant niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, customer)
            }

            put("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig klant-id."))
                    return@put
                }

                val request = call.receive<UpdateCustomerRequest>()

                when (val result = customerService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status =
                            if (result.message == "Klant niet gevonden.") HttpStatusCode.NotFound
                            else HttpStatusCode.BadRequest

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            delete("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig klant-id."))
                    return@delete
                }

                when (val result = customerService.delete(id)) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Klant succesvol verwijderd.")
                    )

                    is ServiceResult.Error -> call.respond(
                        HttpStatusCode.NotFound,
                        ErrorResponse(result.message)
                    )
                }
            }
        }
    }
}