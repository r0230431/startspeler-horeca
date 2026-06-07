package com.startspeler.horeca.routes

import com.startspeler.horeca.database.enums.PaymentMethod
import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.payment.CreatePaymentRequest
import com.startspeler.horeca.service.PaymentService
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route

fun Route.paymentRoutes(paymentService: PaymentService) {
    authenticate("auth-jwt") {
        route("/payments") {

            post {
                val request = call.receive<CreatePaymentRequest>()

                when (val result = paymentService.create(request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                    is ServiceResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(result.message)
                    )
                }
            }

            get("/summary") {
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]

                when (val result = paymentService.getSummary(from, to)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(result.message)
                    )
                }
            }

            get {
                val from = call.request.queryParameters["from"]
                val to = call.request.queryParameters["to"]
                val paymentMethod = call.request.queryParameters["paymentMethod"]?.let {
                    try {
                        PaymentMethod.valueOf(it.uppercase())
                    } catch (e: Exception) {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Ongeldige betaalmethode. Gebruik CASH, BANCONTACT of PAYCONIQ.")
                        )
                        return@get
                    }
                }

                when (val result = paymentService.getAllFiltered(from, to, paymentMethod)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> call.respond(HttpStatusCode.BadRequest, ErrorResponse(result.message))
                }
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig payment-id."))
                    return@get
                }

                val payment = paymentService.getById(id)
                if (payment == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Betaling niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, payment)
            }
        }
    }
}