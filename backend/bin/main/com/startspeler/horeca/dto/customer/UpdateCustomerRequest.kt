package com.startspeler.horeca.dto.customer

import kotlinx.serialization.Serializable

@Serializable
data class UpdateCustomerRequest(
    val username: String
)