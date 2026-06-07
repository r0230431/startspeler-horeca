package com.startspeler.horeca.dto.customer

import kotlinx.serialization.Serializable

@Serializable
data class CustomerResponse(
    val id: Int,
    val username: String
)