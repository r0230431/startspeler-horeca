package com.startspeler.horeca.dto.common

sealed class ServiceResult<out T> {
    data class Success<T>(val data: T) : ServiceResult<T>()
    data class Error(val message: String) : ServiceResult<Nothing>()
}