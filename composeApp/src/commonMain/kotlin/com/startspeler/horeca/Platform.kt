package com.startspeler.horeca

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform