package tg.sdk.sca.presentation.core.ui

import android.os.Bundle
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.data.common.network.BaseError
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.presentation.core.customview.MessageView
import tg.sdk.sca.presentation.utils.extensions.UNDEFINED_INT


abstract class BaseActivity : FragmentActivity() {

    private val messageView: MessageView by lazy { MessageView(this) }

    lateinit var viewModelFactory: ViewModelProvider.Factory

    protected val commonNetworkErrorObserver: (BaseError) -> Unit =
        { handleCommonNetworkErrors(it) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setWindowBackground()
        inflateInfoView()
    }

    protected open fun setWindowBackground() {}


    private fun inflateInfoView() {
        findViewById<ViewGroup>(android.R.id.content).addView(messageView)
    }

    fun handleCommonNetworkErrors(cause: BaseError) {
        when (cause) {
            is NetworkError.Authentication -> handleAuthenticatedError()
            is NetworkError.InvalidRequest -> handleInvalidRequestError()
            is NetworkError.Unauthorized -> showServerErrorDialog(cause) //logout() todo verify
            is NetworkError.Connection -> handleConnectionError()
            is NetworkError.ConnectionTimeout -> handleConnectionTimeoutError()
            is NetworkError.ServerInternalError,
            is NetworkError.ServerTemporaryUnavailable,
            is NetworkError.ServerMaintenance -> showServerErrorDialog(cause)
            else -> cause.message?.let { showInfoMessage(it) }
        }
    }

    private fun showServerErrorDialog(error: BaseError) =
        showInfoMessage(error.message ?: getString(R.string.error_generic))

    private fun handleConnectionTimeoutError() =
        showInfoMessage(getString(R.string.network_error_connection_timeout))

    private fun handleConnectionError() =
        showInfoMessage(getString(R.string.network_error_connection))

    private fun handleAuthenticatedError() =
        showInfoMessage(getString(R.string.error_message_authenticate))

    private fun handleInvalidRequestError() =
        showInfoMessage(getString(R.string.error_generic))

    fun showInfoMessage(message: CharSequence) =
        messageView.show(message)

    protected fun <T : ViewModel> obtainViewModel(vmClass: Class<T>) =
        ViewModelProvider(this, viewModelFactory).get(vmClass)

}