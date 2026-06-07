package com.startspeler.horeca.data.models.customers

import kotlinx.serialization.Serializable

@Serializable
data class CustomerResponse(
    val id: Int,
    val username: String
)