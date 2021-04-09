package tg.sdk.sca.presentation.core.viewmodel

import android.content.Context
import android.text.Editable
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ViewModel
import androidx.lifecycle.observe
import kotlinx.coroutines.Job
import tg.sdk.sca.data.common.network.BaseError
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.common.network.Result
import tg.sdk.sca.presentation.utils.SdkConstants
import tg.sdk.sca.presentation.utils.extensions.event

abstract class BaseViewModel : ViewModel() {

    protected val IO by lazy { CoroutineDispatcherProvider.getUseCaseDispatcher() }
    protected val MAIN by lazy { CoroutineDispatcherProvider.getMainDispatcher() }
    protected val COMPUTITION by lazy { CoroutineDispatcherProvider.getComputationDispatcher() }

    private val commonNetworkError = event<NetworkError>()

    protected fun handleCommonErrors(
        error: BaseError,
        otherErrorsHandler: (cause: BaseError) -> Unit = {}
    ) {
        when (error) {
            is NetworkError.Unauthorized,
            is NetworkError.ServerInternalError,
            is NetworkError.ServerTemporaryUnavailable,
            is NetworkError.ServerMaintenance,
            is NetworkError.Connection,
            is NetworkError.ConnectionTimeout -> commonNetworkError.postValue(error as NetworkError)
            else -> otherErrorsHandler(error)
        }
    }

    fun observeCommonErrors(owner: LifecycleOwner, observer: (BaseError) -> Unit) {
        commonNetworkError.observe(owner, observer)
    }

    fun Job?.cancelIfActive() {
        if (this != null && isActive) cancel()
    }

    protected inline fun <T> handleResult(
            result: Result<T?>,
            onSuccess: (T?) -> Unit = {},
            crossinline onError: (BaseError) -> Unit = {}
    ) {
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error ->  {
                handleCommonErrors(result.error)
                onError(result.error)
            }
        }
    }

    var skipBiometric: Boolean? = null

    fun isEnrolledUsingPin(context: Context): Boolean {
        skipBiometric?.let {
            return it
        }

        skipBiometric = context.getSharedPreferences(SdkConstants.SCA_SDK_SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
            .getBoolean(SdkConstants.KEY_PREF_BIOMETRIC_SKIP, false)
        return skipBiometric!!
    }

    fun isValidPin(pin: Editable?): Boolean {
        return !pin.isNullOrEmpty() && pin.length == 4
    }
}