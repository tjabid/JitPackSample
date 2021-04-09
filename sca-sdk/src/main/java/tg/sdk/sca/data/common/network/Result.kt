package tg.sdk.sca.data.common.network

private const val ERROR_AUTHENTICATION = "Failed to authenticate"
private const val ERROR_INVALID_REQUEST = "Bad request"
private const val ERROR_UNAUTHORIZED = "Unauthorized"
private const val ERROR_VERSION_UPDATE = "Version Update"
private const val ERROR_CONNECTION = "Connection Error"
private const val ERROR_CONNECTION_TIMEOUT = "Connection Timeout"
private const val ERROR_SERVER_INTERNAL = "Something went wrong"
private const val ERROR_SERVER_TEMPORARY_UNAVAILABLE = "Server is temporarily unavailable."
private const val ERROR_SERVER_MAINTENANCE = "Technical works are in progress."

sealed class Result<out R> {

    data class Success<out R>(val data: R?) : Result<R>()
    data class Error(val error: BaseError) : Result<Nothing>()
    /**
     * @param[progress] - 0...1.0
     */
    data class Progress(val progress: Double) : Result<Nothing>()

    override fun toString(): String {
        return when (this) {
            is Success -> "Success[data=$data]"
            is Error -> "Error[exception=$error]"
            is Progress -> "Progress[exception=$progress]"
        }
    }

    fun <F> map(mapFunc: (type: R?) -> F?): Result<F> {
        return when (this) {
            is Success<R> -> Success(
                mapFunc(this.data)
            )
            is Error -> Error(
                this.error
            )
            is Progress -> Progress(
                this.progress
            )
        }
    }
}

abstract class BaseError(message: String = "", cause: Throwable? = null) : Throwable(message)

sealed class NetworkError(errorMessage: String, cause: Throwable? = null) : BaseError(errorMessage, cause) {
    object Authentication : NetworkError(ERROR_AUTHENTICATION)
    object InvalidRequest : NetworkError(ERROR_INVALID_REQUEST)
    object Unauthorized : NetworkError(ERROR_UNAUTHORIZED)
    object Connection : NetworkError(ERROR_CONNECTION)
    object ConnectionTimeout : NetworkError(ERROR_CONNECTION_TIMEOUT)
    object ServerInternalError : NetworkError(ERROR_SERVER_INTERNAL)
    object ServerTemporaryUnavailable : NetworkError(ERROR_SERVER_TEMPORARY_UNAVAILABLE)
    object ServerMaintenance : NetworkError(ERROR_SERVER_MAINTENANCE)
    data class OperationCode(val opCode: String, val responseCode: Int) : NetworkError(opCode)
    data class Unknown(val errorMessage: String = "", override val cause: Throwable? = null) : NetworkError(errorMessage, cause)
}