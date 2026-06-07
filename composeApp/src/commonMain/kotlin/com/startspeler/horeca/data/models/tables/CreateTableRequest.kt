package com.startspeler.horeca.data.models.tables

import kotlinx.serialization.Serializable

@Serializable
data class CreateTableRequest(
    val tableNumber: Int,
    val seatCount: Int,
    val note: String? = null,
)
