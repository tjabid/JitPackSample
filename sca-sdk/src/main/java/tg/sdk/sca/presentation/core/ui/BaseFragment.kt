package tg.sdk.sca.presentation.core.ui

import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.annotation.IdRes
import androidx.annotation.LayoutRes
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.findNavController
import com.miracl.trust.MiraclTrust
import tg.sdk.sca.R
import tg.sdk.sca.data.common.network.BaseError
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.databinding.ComponentToolbarBinding
import tg.sdk.sca.presentation.core.NavControllerProvider
import tg.sdk.sca.presentation.core.customview.InfoView
import tg.sdk.sca.presentation.core.viewmodel.BaseViewModel
import tg.sdk.sca.presentation.utils.ConnectionUtil
import tg.sdk.sca.tgbobf.TgBobfSdk

abstract class BaseFragment: Fragment() {

    lateinit var viewModelFactory: ViewModelProvider.Factory

    @get:LayoutRes
    abstract val layout: Int

    open val viewModel: BaseViewModel? = null

    private val infoView: InfoView by lazy { InfoView(requireContext()) }

    protected open val commonNetworkErrorObserver: (BaseError) -> Unit = { error ->
        handleCommonNetworkErrors(error)
    }

    val navController: NavController
        get() = findNavController()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        val binding: ViewDataBinding = DataBindingUtil.inflate(
                inflater, layout, container, false)
        (activity as? NavControllerProvider)?.navController?.let {
            Navigation.setViewNavController(
                    binding.root,
                    it
            )
        }
        castBinding(binding)
        return binding.root
    }

    abstract fun castBinding(viewBinding: ViewDataBinding)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        inflateInfoView()
        setupViews()
        observeCommonErrors()
        checkMiraclConfiguration()
    }

    // todo temporary fix - improve with internet re-connectivity
    private fun checkMiraclConfiguration() {
        try {
            MiraclTrust.getInstance()
        } catch (e: Exception) {
            context?.let {
                if (ConnectionUtil.isNetworkAvailable(it)) {
                    TgBobfSdk.configurationMiracl(it)
                }
            }
        }
    }

    open fun observeCommonErrors() {
        viewModel?.observeCommonErrors(viewLifecycleOwner, commonNetworkErrorObserver)
    }

    private fun inflateInfoView() {
        if (infoView.parent == null) {
            activity?.findViewById<ViewGroup>(android.R.id.content)?.addView(infoView)
        }
    }

    protected fun showKeyboard() =
        (context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)

    protected fun hideKeyboard() =
        (context?.getSystemService(Activity.INPUT_METHOD_SERVICE) as? InputMethodManager)
            ?.hideSoftInputFromWindow(requireView().windowToken, 0)

    fun showSuccessMessage(title: CharSequence, subtitle: CharSequence? = null) =
        infoView.showSuccessMessage(title, subtitle)

    fun showErrorMessage(title: CharSequence, subtitle: CharSequence? = null) =
        infoView.showErrorMessage(title, subtitle)

    protected fun <T : ViewModel> obtainViewModel(
        vmClass: Class<T>,
        owner: ViewModelStoreOwner = this
    ) = ViewModelProvider(owner, viewModelFactory).get(vmClass)

    abstract fun setupViews()

    fun setupToolbar(viewBinding: ComponentToolbarBinding, text: String, function: (() -> Unit)? = null) {
        viewBinding.tgToolbarBackTitle.text = text
        viewBinding.btnToolbarBack.setOnClickListener {
            function?.invoke() ?: activity?.onBackPressed()
        }
    }

    protected fun navigateSafe(
        @IdRes destinationId: Int,
        @IdRes actionId: Int? = null,
        bundle: Bundle? = null
    ) {
        if (navController.currentDestination?.id != destinationId) {
            navController.navigate(actionId ?: destinationId, bundle)
        }
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
            else -> cause.message?.let { showErrorMessage(it) }
        }
    }

    private fun showServerErrorDialog(error: BaseError) =
        showErrorMessage(error.message ?: getString(R.string.error_generic))

    private fun handleConnectionTimeoutError() =
        showErrorMessage(getString(R.string.network_error_connection_timeout))

    private fun handleConnectionError() =
        showErrorMessage(getString(R.string.network_error_connection))

    private fun handleAuthenticatedError() =
        showErrorMessage(getString(R.string.error_message_authenticate))

    private fun handleInvalidRequestError() =
        showErrorMessage(getString(R.string.error_generic))

}