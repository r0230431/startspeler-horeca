package com.startspeler.horeca.service

import com.startspeler.horeca.dto.auth.LoginRequest
import com.startspeler.horeca.dto.auth.LoginResponse
import com.startspeler.horeca.repository.CrewMemberRepository
import com.startspeler.horeca.security.JwtConfig
import com.startspeler.horeca.security.PasswordHasher

class AuthService(
    private val crewMemberRepository: CrewMemberRepository,
    private val passwordHasher: PasswordHasher,
    private val jwtConfig: JwtConfig
) {
    fun login(request: LoginRequest): LoginResponse? {
        val crewMember = crewMemberRepository.findByUsername(request.username.trim())
            ?: return null

        val isValidPassword = passwordHasher.verify(
            request.password,
            crewMember.passwordHash
        )

        if (!isValidPassword) return null

        val token = jwtConfig.generateToken(
            crewMemberId = crewMember.id,
            username = crewMember.username,
            role = crewMember.role
        )

        return LoginResponse(
            token = token,
            crewMemberId = crewMember.id,
            username = crewMember.username,
            role = crewMember.role.name
        )
    }
}