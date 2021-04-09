package tg.sdk.sca.data.common.network

import retrofit2.Response

object ResponseHandler {

    private val networkErrorMapper: NetworkErrorMapper by lazy { NetworkErrorMapper() }

    fun <T> handleResponse(response: Response<T>) =
        when {
            response.isSuccessful -> {
                response.body()?.let { Result.Success(it) } ?: Result.Success(null)
            }
            else -> Result.Error(networkErrorMapper.toErrorCause(response = response))
        }

    fun handleFailure(t: Throwable) =
        Result.Error(networkErrorMapper.toErrorCause<Any>(throwable = t))

}