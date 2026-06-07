package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.MessageResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.discount.CreateDiscountRequest
import com.startspeler.horeca.dto.discount.UpdateDiscountRequest
import com.startspeler.horeca.service.DiscountService
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

fun Route.discountRoutes(discountService: DiscountService) {
    route("/discounts") {
        authenticate("auth-jwt") {

            get {
                call.respond(HttpStatusCode.OK, discountService.getAll())
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig korting-id."))
                    return@get
                }

                val discount = discountService.getById(id)
                if (discount == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Korting niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, discount)
            }

            post {
                if (!call.requireAdmin()) return@post

                val request = call.receive<CreateDiscountRequest>()

                when (val result = discountService.create(request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                    is ServiceResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(result.message)
                    )
                }
            }

            put("/{id}") {
                if (!call.requireAdmin()) return@put

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig korting-id."))
                    return@put
                }

                val request = call.receive<UpdateDiscountRequest>()

                when (val result = discountService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status =
                            if (result.message == "Korting niet gevonden.") HttpStatusCode.NotFound
                            else HttpStatusCode.BadRequest

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            delete("/{id}") {
                if (!call.requireAdmin()) return@delete

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig korting-id."))
                    return@delete
                }

                when (val result = discountService.delete(id)) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Korting succesvol verwijderd.")
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