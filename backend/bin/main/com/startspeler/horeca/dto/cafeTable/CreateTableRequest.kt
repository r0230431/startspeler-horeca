package com.startspeler.horeca.dto.table

import kotlinx.serialization.Serializable

@Serializable
data class CreateTableRequest(
    val tableNumber: Int,
    val seatCount: Int,
    val note: String? = null,
)