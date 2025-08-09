package org.techascent.shared.network

sealed class ResultState<out T> {
    data class Success<out T>(val data: T) : ResultState<T>()
    data class Error(val message: String?, val throwable: Throwable? = null) : ResultState<Nothing>()
    object Loading : ResultState<Nothing>()
}

enum class Status {
    LOADING, SUCCESS, ERROR
}