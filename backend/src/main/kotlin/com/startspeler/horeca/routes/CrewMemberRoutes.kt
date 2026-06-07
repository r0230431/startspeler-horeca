package com.startspeler.horeca.routes

import com.startspeler.horeca.dto.common.ErrorResponse
import com.startspeler.horeca.dto.common.MessageResponse
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.crew.ChangePasswordRequest
import com.startspeler.horeca.dto.crew.CreateCrewMemberRequest
import com.startspeler.horeca.dto.crew.UpdateCrewMemberRequest
import com.startspeler.horeca.security.UserPrincipal
import com.startspeler.horeca.service.ChangePasswordResult
import com.startspeler.horeca.service.CrewMemberService
import com.startspeler.horeca.util.requireAdmin
import io.ktor.http.*
import io.ktor.server.auth.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*

fun Route.crewMemberRoutes(crewMemberService: CrewMemberService) {
    authenticate("auth-jwt") {
        route("/crew-members") {

            get {
                if (!call.requireAdmin()) return@get
                call.respond(HttpStatusCode.OK, crewMemberService.getAll())
            }

            get("/{id}") {
                if (!call.requireAdmin()) return@get

                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig medewerker-id."))
                    return@get
                }

                val crewMember = crewMemberService.getById(id)
                if (crewMember == null) {
                    call.respond(HttpStatusCode.NotFound, ErrorResponse("Medewerker niet gevonden."))
                    return@get
                }

                call.respond(HttpStatusCode.OK, crewMember)
            }

            post {
                if (!call.requireAdmin()) return@post

                val request = call.receive<CreateCrewMemberRequest>()

                when (val result = crewMemberService.create(request)) {
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
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig medewerker-id."))
                    return@put
                }

                val request = call.receive<UpdateCrewMemberRequest>()

                when (val result = crewMemberService.update(id, request)) {
                    is ServiceResult.Success -> call.respond(HttpStatusCode.OK, result.data)
                    is ServiceResult.Error -> {
                        val status = if (result.message == "Medewerker niet gevonden.") {
                            HttpStatusCode.NotFound
                        } else {
                            HttpStatusCode.BadRequest
                        }

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            delete("/{id}") {
                if (!call.requireAdmin()) return@delete

                val principal = call.principal<UserPrincipal>()
                val id = call.parameters["id"]?.toIntOrNull()
                if (id == null) {
                    call.respond(HttpStatusCode.BadRequest, ErrorResponse("Ongeldig medewerker-id."))
                    return@delete
                }

                if (principal?.crewMemberId == id) {
                    call.respond(
                        HttpStatusCode.BadRequest,
                        ErrorResponse("Je kan je eigen account niet verwijderen.")
                    )
                    return@delete
                }

                when (val result = crewMemberService.delete(id)) {
                    is ServiceResult.Success -> call.respond(
                        HttpStatusCode.OK,
                        MessageResponse("Medewerker succesvol verwijderd.")
                    )

                    is ServiceResult.Error -> {
                        val status = when (result.message) {
                            "Medewerker niet gevonden." -> HttpStatusCode.NotFound
                            "De laatste administrator kan niet verwijderd worden." -> HttpStatusCode.BadRequest
                            else -> HttpStatusCode.BadRequest
                        }

                        call.respond(status, ErrorResponse(result.message))
                    }
                }
            }

            patch("/me/password") {
                val principal = call.principal<UserPrincipal>()
                if (principal == null) {
                    call.respond(
                        HttpStatusCode.Unauthorized,
                        ErrorResponse("Niet ingelogd.")
                    )
                    return@patch
                }

                val request = call.receive<ChangePasswordRequest>()

                when (crewMemberService.changeOwnPassword(principal.crewMemberId, request)) {
                    ChangePasswordResult.SUCCESS -> {
                        call.respond(
                            HttpStatusCode.OK,
                            MessageResponse("Wachtwoord succesvol gewijzigd.")
                        )
                    }

                    ChangePasswordResult.INVALID_INPUT -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Huidig wachtwoord en nieuw wachtwoord zijn verplicht.")
                        )
                    }

                    ChangePasswordResult.NEW_PASSWORD_TOO_SHORT -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Nieuw wachtwoord moet minstens 8 karakters bevatten.")
                        )
                    }

                    ChangePasswordResult.USER_NOT_FOUND -> {
                        call.respond(
                            HttpStatusCode.NotFound,
                            ErrorResponse("Medewerker niet gevonden.")
                        )
                    }

                    ChangePasswordResult.CURRENT_PASSWORD_INCORRECT -> {
                        call.respond(
                            HttpStatusCode.BadRequest,
                            ErrorResponse("Huidig wachtwoord is onjuist.")
                        )
                    }

                    ChangePasswordResult.UPDATE_FAILED -> {
                        call.respond(
                            HttpStatusCode.InternalServerError,
                            ErrorResponse("Wachtwoord kon niet worden gewijzigd.")
                        )
                    }
                }
            }
        }
    }
}