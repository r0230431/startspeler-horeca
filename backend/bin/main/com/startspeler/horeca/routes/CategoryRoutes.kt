package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.category.CreateCategoryRequest
import com.startspeler.horeca.dto.category.UpdateCategoryRequest
import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.MessageResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.service.CategoryService
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

fun Route.categoryRoutes(categoryService: CategoryService) {
    route("/categories") {

        get {
            call.respond(HttpStatusCode.OK, categoryService.getAll())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig categorie-id."))
                return@get
            }

            val category = categoryService.getById(id)
            if (category == null) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Categorie niet gevonden."))
                return@get
            }

            call.respond(HttpStatusCode.OK, category)
        }
    }

    authenticate("auth-jwt") {
        route("/categories") {

            post {
                if (!call.requireAdmin()) return@post

                val request = call.receive<CreateCategoryRequest>()

                when (val result = categoryService.create(request)) {
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
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig categorie-id."))
                    return@put
                }

                val request = call.receive<UpdateCategoryRequest>()

                when (val result = categoryService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status =
                            if (result.message == "Categorie niet gevonden.") HttpStatusCode.NotFound
                            else HttpStatusCode.BadRequest

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            delete("/{id}") {
                if (!call.requireAdmin()) return@delete

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig categorie-id."))
                    return@delete
                }

                when (val result = categoryService.delete(id)) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Categorie succesvol verwijderd.")
                    )

                    is ServiceResult.Error -> {
                        val status =
                            if (result.message == "Categorie niet gevonden.") HttpStatusCode.NotFound
                            else HttpStatusCode.BadRequest

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }
        }
    }
}