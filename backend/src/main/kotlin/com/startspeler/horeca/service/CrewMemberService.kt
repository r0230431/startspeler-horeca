package com.startspeler.horeca.service

import com.startspeler.horeca.database.enums.CrewRole
import com.startspeler.horeca.dto.common.ServiceResult
import com.startspeler.horeca.dto.crew.ChangePasswordRequest
import com.startspeler.horeca.dto.crew.CreateCrewMemberRequest
import com.startspeler.horeca.dto.crew.CrewMemberResponse
import com.startspeler.horeca.dto.crew.UpdateCrewMemberRequest
import com.startspeler.horeca.repository.CrewMemberRepository
import com.startspeler.horeca.security.PasswordHasher

class CrewMemberService(
    private val crewMemberRepository: CrewMemberRepository,
    private val passwordHasher: PasswordHasher
) {

    fun getAll(): List<CrewMemberResponse> =
        crewMemberRepository.findAll()

    fun getById(id: Int): CrewMemberResponse? =
        crewMemberRepository.findById(id)

    fun create(request: CreateCrewMemberRequest): ServiceResult<CrewMemberResponse> {
        val trimmedUsername = request.username.trim()
        val passwordValidationError = validateNewPassword(request.password)

        if (trimmedUsername.isBlank()) {
            return ServiceResult.Error("Gebruikersnaam is verplicht.")
        }

        if (passwordValidationError != null) {
            return ServiceResult.Error(passwordValidationError)
        }

        val existing = crewMemberRepository.findByUsername(trimmedUsername)
        if (existing != null) {
            return ServiceResult.Error("Er bestaat al een medewerker met deze gebruikersnaam.")
        }

        val passwordHash = passwordHasher.hash(request.password)

        val created = crewMemberRepository.create(
            username = trimmedUsername,
            passwordHash = passwordHash,
            role = request.role
        )

        return ServiceResult.Success(created)
    }

    fun update(id: Int, request: UpdateCrewMemberRequest): ServiceResult<CrewMemberResponse> {
        val existing = crewMemberRepository.findById(id)
            ?: return ServiceResult.Error("Medewerker niet gevonden.")

        val trimmedUsername = request.username.trim()

        if (trimmedUsername.isBlank()) {
            return ServiceResult.Error("Gebruikersnaam is verplicht.")
        }

        if (existing.role == CrewRole.ADMIN && request.role != CrewRole.ADMIN) {
            val adminCount = crewMemberRepository.countAdmins()
            if (adminCount <= 1) {
                return ServiceResult.Error("De laatste administrator kan niet gewijzigd worden naar medewerker.")
            }
        }

        val userWithSameUsername = crewMemberRepository.findByUsername(trimmedUsername)
        if (userWithSameUsername != null && userWithSameUsername.id != id) {
            return ServiceResult.Error("Er bestaat al een medewerker met deze gebruikersnaam.")
        }

        val normalizedPassword = request.password?.trim()?.takeIf { it.isNotEmpty() }
        if (request.password != null) {
            val passwordValidationError = validateNewPassword(normalizedPassword)
            if (passwordValidationError != null) {
                return ServiceResult.Error(passwordValidationError)
            }
        }

        val passwordHash = normalizedPassword?.let(passwordHasher::hash)

        crewMemberRepository.update(
            id = id,
            username = trimmedUsername,
            role = request.role,
            passwordHash = passwordHash,
        )

        val updated = crewMemberRepository.findById(id) ?: existing
        return ServiceResult.Success(updated)
    }

    fun delete(id: Int): ServiceResult<Unit> {
        val existing = crewMemberRepository.findById(id)
            ?: return ServiceResult.Error("Medewerker niet gevonden.")

        if (existing.role == CrewRole.ADMIN) {
            val adminCount = crewMemberRepository.countAdmins()
            if (adminCount <= 1) {
                return ServiceResult.Error("De laatste administrator kan niet verwijderd worden.")
            }
        }

        crewMemberRepository.delete(existing.id)
        return ServiceResult.Success(Unit)
    }

    fun changeOwnPassword(
        crewMemberId: Int,
        request: ChangePasswordRequest
    ): ChangePasswordResult {
        if (request.currentPassword.isBlank() || request.newPassword.isBlank()) {
            return ChangePasswordResult.INVALID_INPUT
        }

        val newPasswordValidationError = validateNewPassword(request.newPassword)
        if (newPasswordValidationError == "Wachtwoord moet minstens 8 karakters bevatten.") {
            return ChangePasswordResult.NEW_PASSWORD_TOO_SHORT
        }
        if (newPasswordValidationError != null) {
            return ChangePasswordResult.INVALID_INPUT
        }

        val crewMember = crewMemberRepository.findAuthById(crewMemberId)
            ?: return ChangePasswordResult.USER_NOT_FOUND

        val currentPasswordMatches = passwordHasher.verify(
            request.currentPassword,
            crewMember.passwordHash
        )

        if (!currentPasswordMatches) {
            return ChangePasswordResult.CURRENT_PASSWORD_INCORRECT
        }

        val newPasswordHash = passwordHasher.hash(request.newPassword)

        val updated = crewMemberRepository.updatePasswordHash(
            crewMemberId = crewMemberId,
            newPasswordHash = newPasswordHash
        )

        return if (updated) {
            ChangePasswordResult.SUCCESS
        } else {
            ChangePasswordResult.UPDATE_FAILED
        }
    }

    private fun validateNewPassword(password: String?): String? {
        if (password.isNullOrBlank()) {
            return "Wachtwoord is verplicht."
        }

        if (password.length < 8) {
            return "Wachtwoord moet minstens 8 karakters bevatten."
        }

        if (!password.any(Char::isLetter) || !password.any(Char::isDigit)) {
            return "Wachtwoord moet minstens één letter en één cijfer bevatten."
        }

        return null
    }
}

enum class ChangePasswordResult {
    SUCCESS,
    INVALID_INPUT,
    NEW_PASSWORD_TOO_SHORT,
    USER_NOT_FOUND,
    CURRENT_PASSWORD_INCORRECT,
    UPDATE_FAILED
}
