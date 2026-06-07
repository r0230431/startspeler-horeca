package com.startspeler.horeca.screens.crew.management.crew

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.startspeler.horeca.data.api.CrewMemberApi
import com.startspeler.horeca.data.models.common.ApiResult
import com.startspeler.horeca.data.models.crew.CreateCrewMemberRequest
import com.startspeler.horeca.data.models.crew.CrewMemberResponse
import com.startspeler.horeca.data.models.crew.UpdateCrewMemberRequest

class CrewMembersViewModel(
    private val crewMemberApi: CrewMemberApi = CrewMemberApi(),
) {
    var crewMembers by mutableStateOf<List<CrewMemberResponse>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    suspend fun loadCrewMembers() {
        isLoading = true
        errorMessage = null
        try {
            crewMembers = crewMemberApi.getCrewMembers().sortedBy { it.username.lowercase() }
        } catch (e: Exception) {
            errorMessage = e.message ?: "Medewerkers konden niet geladen worden."
        } finally {
            isLoading = false
        }
    }

    suspend fun createCrewMember(request: CreateCrewMemberRequest): ApiResult<CrewMemberResponse> {
        val result = crewMemberApi.createCrewMember(request)
        if (result is ApiResult.Success) {
            loadCrewMembers()
        }
        return result
    }

    suspend fun updateCrewMember(id: Int, request: UpdateCrewMemberRequest): ApiResult<CrewMemberResponse> {
        val result = crewMemberApi.updateCrewMember(id, request)
        if (result is ApiResult.Success) {
            loadCrewMembers()
        }
        return result
    }

    suspend fun deleteCrewMember(id: Int): ApiResult<Unit> {
        val result = crewMemberApi.deleteCrewMember(id)
        if (result is ApiResult.Success) {
            loadCrewMembers()
        }
        return result
    }

    fun clearError() {
        errorMessage = null
    }
}
