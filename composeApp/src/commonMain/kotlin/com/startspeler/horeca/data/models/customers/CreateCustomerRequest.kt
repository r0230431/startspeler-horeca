package com.startspeler.horeca.data.models.customers

import kotlinx.serialization.Serializable

@Serializable
data class CreateCustomerRequest(
    val username: String
)