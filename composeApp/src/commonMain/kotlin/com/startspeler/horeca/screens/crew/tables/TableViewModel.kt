package com.startspeler.horeca.screens.crew.tables

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.TableApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.tables.CreateTableRequest
import com.startspeler.horeca.data.models.tables.TableResponse
import com.startspeler.horeca.data.models.tables.UpdateTableRequest

class TableViewModel(
    private val tableApi: TableApi = TableApi(),
) {
    var tables by mutableStateOf<List<TableResponse>>(emptyList())
        private set

    var selectedTable by mutableStateOf<TableResponse?>(null)
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadTables() {
        isLoading = true
        errorMessage = null
        try {
            tables = tableApi.getTables().sortedBy { it.tableNumber }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Tafels konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun loadTable(id: Int) {
        isLoading = true
        errorMessage = null
        try {
            selectedTable = tableApi.getTableById(id)
        } catch (e: Exception) {
            errorMessage = e.message ?: "Tafeldetails konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun createTable(
        tableNumber: Int,
        seatCount: Int,
        note: String?,
    ): ApiResult<TableResponse> {
        val result = tableApi.createTable(
            CreateTableRequest(
                tableNumber = tableNumber,
                seatCount = seatCount,
                note = note?.trim()?.ifBlank { null },
            )
        )

        if (result is ApiResult.Success) {
            loadTables()
        }

        return result
    }

    suspend fun updateTable(
        id: Int,
        tableNumber: Int,
        seatCount: Int,
        note: String?,
    ): ApiResult<TableResponse> {
        val result = tableApi.updateTable(
            id = id,
            request = UpdateTableRequest(
                tableNumber = tableNumber,
                seatCount = seatCount,
                note = note?.trim()?.ifBlank { null },
            )
        )

        if (result is ApiResult.Success) {
            selectedTable = result.data
            loadTables()
        }

        return result
    }

    suspend fun deleteTable(id: Int): ApiResult<Unit> {
        val result = tableApi.deleteTable(id)
        if (result is ApiResult.Success) {
            if (selectedTable?.id == id) selectedTable = null
            loadTables()
        }
        return result
    }

    fun clearError() {
        errorMessage = null
    }
}
