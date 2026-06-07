package com.startspeler.horeca.dto.table

import kotlinx.serialization.Serializable

@Serializable
data class TableResponse(
    val id: Int,
    val tableNumber: Int,
    val seatCount: Int,
    val note: String?,
)