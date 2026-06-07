package com.startspeler.horeca.security

import at.favre.lib.crypto.bcrypt.BCrypt

class PasswordHasher {

    fun hash(password: String): String {
        return BCrypt.withDefaults().hashToString(12, password.toCharArray())
    }

    fun verify(password: String, passwordHash: String): Boolean {
        return BCrypt.verifyer().verify(password.toCharArray(), passwordHash).verified
    }
}