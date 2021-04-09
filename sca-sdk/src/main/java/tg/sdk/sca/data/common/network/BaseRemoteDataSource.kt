package tg.sdk.sca.data.common.network

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

/**
 * Abstract Base Data source class with error handling
 */
abstract class BaseRemoteDataSource(
    private val networkErrorMapper: NetworkErrorMapper,
    private val failureRequestsManager: FailureRequestsManager
) : BaseService {

    @ExperimentalCoroutinesApi
    protected fun <T> Call<T>.asFlow(): Flow<Result<T>> = callbackFlow {
        val scope: CoroutineScope = this
        var isFlowCancelled = false
        val callback = object : Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                if (isCanceled) return
                val cb = this
                scope.launch {
                    val handledResponse = handleResponse(response, call, cb)
                    offer(handledResponse)
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                if (isFlowCancelled) return
                offer(handleFailure(t))
            }
        }
        if (!isExecuted && !isCanceled) {
            enqueue(callback)
        }
        awaitClose {
            isFlowCancelled = true
            cancel()
        }
    }

    protected fun <T> Call<T>.asResult(action: (Result<T>) -> Unit) {
        val callback = object : Callback<T> {
            override fun onResponse(
                call: Call<T>,
                response: Response<T>
            ) {
                var cb = this
                CoroutineScope(Dispatchers.Unconfined).launch {
                    action(handleResponse(response, call, cb))
                }
            }

            override fun onFailure(call: Call<T>, t: Throwable) {
                action(handleFailure(t))
            }
        }
        enqueue(callback)
    }

    private fun <T> handleResponse(
        response: Response<T>,
        call: Call<T>,
        callback: Callback<T>
    ) =
        when {
            response.isSuccessful -> response.body()?.let {
                Result.Success(it)
            } ?: Result.Success(null)
            else -> {
                var error =
                    networkErrorMapper.toErrorCause(response = response)
                when (error) {
                    is NetworkError.ServerInternalError ->
                        failureRequestsManager.serviceUnavailableFailureRetrier.putCloned(
                            call,
                            callback
                        )
                }
                Result.Error(error)
            }
        }

    private fun handleFailure(t: Throwable) =
        Result.Error(networkErrorMapper.toErrorCause<Any>(throwable = t))

    override fun clearFailedServiceUnavailableRequests() =
        failureRequestsManager.serviceUnavailableFailureRetrier.clear()

    override fun retryFailedServiceUnavailableRequests() =
        failureRequestsManager.serviceUnavailableFailureRetrier.retry()

    private fun <T> FailureRequestsRetrier.putCloned(call: Call<T>, callback: Callback<T>) {
        val headers = call.request().headers
        val clonedCall = call.clone()
        clonedCall.request().headers.newBuilder()
            .addAll(headers)
            .build()
        put(clonedCall, callback)
    }
}