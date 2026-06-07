package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.MessageResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.table.CreateTableRequest
import com.startspeler.horeca.dto.table.UpdateTableRequest
import com.startspeler.horeca.service.CafeTableService
import com.startspeler.horeca.util.requireAdmin
import io.ktor.http.HttpStatusCode
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.*

fun Route.cafeTableRoutes(tableService: CafeTableService) {

    route("/public/tables") {
        get("/by-number/{tableNumber}") {
            val tableNumber = call.parameters["tableNumber"]?.toIntOrNull()
            if (tableNumber == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig tafelnummer."))
                return@get
            }

            val table = tableService.getByTableNumber(tableNumber)
            if (table == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Tafel niet gevonden."))
                return@get
            }

            call.respond(HttpStatusCode.OK, table)
        }
    }

    route("/tables") {

        authenticate("auth-jwt") {
            get {
                call.respond(HttpStatusCode.OK, tableService.getAll())
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig tafel-id."))
                    return@get
                }

                val table = tableService.getById(id)
                if (table == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Tafel niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, table)
            }

            post {
                if (!call.requireAdmin()) return@post

                val request = call.receive<CreateTableRequest>()

                when (val result = tableService.create(request)) {
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
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig tafel-id."))
                    return@put
                }

                val request = call.receive<UpdateTableRequest>()

                when (val result = tableService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status =
                            if (result.message == "Tafel niet gevonden.") HttpStatusCode.NotFound
                            else HttpStatusCode.BadRequest

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            delete("/{id}") {
                if (!call.requireAdmin()) return@delete

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse( "Ongeldig tafel-id."))
                    return@delete
                }

                when (val result = tableService.delete(id)) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Tafel $id succesvol verwijderd.")
                    )
                    is ServiceResult.Error -> {
                        val status = if (result.message == "Tafel niet gevonden.") {
                            HttpStatusCode.NotFound
                        } else {
                            HttpStatusCode.BadRequest
                        }

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }
        }
    }
}