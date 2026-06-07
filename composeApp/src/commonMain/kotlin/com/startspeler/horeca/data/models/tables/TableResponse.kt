package com.startspeler.horeca.data.models.tables

import kotlinx.serialization.Serializable

@Serializable
data class TableResponse(
    val id: Int,
    val tableNumber: Int,
    val seatCount: Int,
    val note: String? = null
)