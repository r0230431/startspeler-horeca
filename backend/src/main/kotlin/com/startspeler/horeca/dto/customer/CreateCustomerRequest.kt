package com.startspeler.horeca.dto.customer

import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    val username: String
)