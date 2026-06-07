package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.MessageResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.product.AddProductDeliveryRequest
import com.startspeler.horeca.dto.product.CreateProductRequest
import com.startspeler.horeca.dto.product.UpdateProductInventoryRequest
import com.startspeler.horeca.dto.product.UpdateProductRequest
import com.startspeler.horeca.dto.product.UpdateProductStockRequest
import com.startspeler.horeca.service.ProductImageStorage
import com.startspeler.horeca.service.ProductService
import com.startspeler.horeca.util.requireAdmin
import com.startspeler.horeca.util.requireUser
import io.ktor.http.HttpStatusCode
import io.ktor.http.content.PartData
import io.ktor.http.content.forEachPart
import io.ktor.server.auth.authenticate
import io.ktor.server.request.receive
import io.ktor.server.request.receiveMultipart
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.delete
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.utils.io.readRemaining
import kotlinx.io.readByteArray

fun Route.productRoutes(productService: ProductService) {

    route("/public/products") {
        get {
            call.respond(HttpStatusCode.OK, productService.getAllActive())
        }

        get("/{id}") {
            val id = call.parameters["id"]?.toIntOrNull()
            if (id == null) {
                call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                return@get
            }

            val product = productService.getById(id)
            if (product == null || !product.isActive) {
                call.respond(HttpStatusCode.NotFound, ErrorResponse("Product niet gevonden."))
                return@get
            }

            call.respond(HttpStatusCode.OK, product)
        }
    }

    authenticate("auth-jwt") {
        route("/products") {

            get {
                call.respond(HttpStatusCode.OK, productService.getAll())
            }

            get("/{id}") {
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                    return@get
                }

                val product = productService.getById(id)
                if (product == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Product niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, product)
            }

            put("/{id}/stock") {
                if (call.requireUser() == null) return@put

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                    return@put
                }

                val request = call.receive<UpdateProductStockRequest>()

                when (val result = productService.updateStock(id, request.stock)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status = if (result.message == "Product niet gevonden.") {
                            HttpStatusCode.NotFound
                        } else {
                            HttpStatusCode.BadRequest
                        }
                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            put("/{id}/inventory") {
                if (!call.requireAdmin()) return@put

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                    return@put
                }

                val request = call.receive<UpdateProductInventoryRequest>()

                when (val result = productService.updateInventory(id, request.stock, request.minimumStock)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status = if (result.message == "Product niet gevonden.") {
                            HttpStatusCode.NotFound
                        } else {
                            HttpStatusCode.BadRequest
                        }
                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            post("/{id}/deliveries") {
                if (call.requireUser() == null) return@post

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                    return@post
                }

                val request = call.receive<AddProductDeliveryRequest>()

                when (val result = productService.addDelivery(id, request.quantity)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status = if (result.message == "Product niet gevonden.") {
                            HttpStatusCode.NotFound
                        } else {
                            HttpStatusCode.BadRequest
                        }
                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            post("/upload-image") {
                if (!call.requireAdmin()) return@post

                var fileBytes: ByteArray? = null
                var fileName: String? = null
                var contentType: String? = null

                val multipart = call.receiveMultipart(formFieldLimit = ProductImageStorage.MAX_IMAGE_BYTES)
                multipart.forEachPart { part ->
                    when (part) {
                        is PartData.FileItem -> {
                            if (part.name == "file" && fileBytes == null) {
                                fileName = part.originalFileName
                                contentType = part.contentType?.toString()
                                fileBytes = part.provider().readRemaining().readByteArray()
                            }
                        }

                        else -> Unit
                    }
                    part.dispose()
                }

                val bytes = fileBytes
                if (bytes == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Er werd geen afbeelding geselecteerd."))
                    return@post
                }

                when (val result = productService.uploadImage(bytes, fileName, contentType)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.Created, result.data)
                    is ServiceResult.Error -> call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse(result.message)
                    )
                }
            }

            post {
                if (!call.requireAdmin()) return@post

                val request = call.receive<CreateProductRequest>()

                when (val result = productService.create(request)) {
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
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                    return@put
                }

                val request = call.receive<UpdateProductRequest>()

                when (val result = productService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status =
                            if (result.message == "Product niet gevonden.") HttpStatusCode.NotFound
                            else HttpStatusCode.BadRequest

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            delete("/{id}") {
                if (!call.requireAdmin()) return@delete

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig product-id."))
                    return@delete
                }

                when (val result = productService.delete(id)) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Product succesvol verwijderd.")
                    )

                    is ServiceResult.Error -> {
                        val status = if (result.message == "Product niet gevonden.") {
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