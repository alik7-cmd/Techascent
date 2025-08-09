package org.techascent.shared.network

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow

/**
 * Wraps a remote API call and emits a ResultState<TDto>
 *
 * @param onCallRemoteApi The actual suspend API call (returns API response class)
 * @param onMapData Optional mapper from API response class → DTO
 */
inline fun <Response, Dto> baseRemoteCall(
    crossinline onCallRemoteApi: suspend () -> Response,
    crossinline onMapData: (Response) -> Dto
): Flow<ResultState<Dto>> = flow {
    emit(ResultState.Loading)
    val response = onCallRemoteApi()
    val dto = onMapData(response)
    emit(ResultState.Success(dto))
}.catch { e ->
    emit(ResultState.Error(e.message, e))
}