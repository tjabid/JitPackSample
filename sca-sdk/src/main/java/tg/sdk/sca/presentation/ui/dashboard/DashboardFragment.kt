package tg.sdk.sca.presentation.ui.dashboard

import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.databinding.FragmentDashboardBinding
import tg.sdk.sca.presentation.core.ui.AuthenticationBaseFragment
import tg.sdk.sca.presentation.core.viewmodel.TgBobfSdkViewModelFactory
import tg.sdk.sca.presentation.ui.dashboard.DashboardViewModel.DashboardViewState.*
import tg.sdk.sca.presentation.utils.AlertDialogClickListener
import tg.sdk.sca.presentation.utils.SimpleAlertDialogFragment

class DashboardFragment: AuthenticationBaseFragment() {

    override val layout: Int = R.layout.fragment_dashboard

    private lateinit var binding: FragmentDashboardBinding
    override lateinit var viewModel: DashboardViewModel

    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentDashboardBinding
    }

    override fun setupViews() {

        viewModelFactory = TgBobfSdkViewModelFactory(DashboardViewModel())
        viewModel = ViewModelProvider(this, viewModelFactory).get(DashboardViewModel::class.java)

        setupToolbar(binding.toolbarDashboard, getString(R.string.title_dashboard))

        binding.manageConsent.setOnClickListener {
            navController.navigate(DashboardFragmentDirections.actionDashboardToManageConsent())
        }

        binding.deRegistration.setOnClickListener {
            alertDeRegistration()
        }

        viewModel.viewState.observe(this) {
            it?.let { handleViewState(it) }
        }
        viewModel.error.observe(this) {
            it?.let { handleCommonNetworkErrors(it) }
        }
    }

    private fun alertDeRegistration() {
        SimpleAlertDialogFragment.newInstance(
            titleId = R.string.deregister_dialog_title,
            message = getString(R.string.deregister_dialog_message),
            positiveButtonTextId = R.string.button_continue,
            negativeButtonTextId = R.string.button_cancel,
            listener = object: AlertDialogClickListener {
                override fun onPositiveClick(requestCode: Int) {
                    super.onPositiveClick(requestCode)
                    startDeRegistration()
                }
            },
            isCancelable = true
        ).show(requireActivity().supportFragmentManager,
            SimpleAlertDialogFragment.TAG
        )
    }

    private fun startDeRegistration() {
        //todo show alert dialog
        if (canAuthenticateWithBiometric()) {
            viewModel.startBiometric()
        } else {
            viewModel.startPinEntry()
        }
    }

    private fun handleViewState(state: DashboardViewModel.DashboardViewState) {
        showProgress(
            when (state) {
                INITIAL_VIEW -> false
                PIN_ENTRY -> {
                    showPinInput()
                    false
                }
                BIOMETRIC -> {
                    showBiometricPromptForDecryption()
                    false
                }
                AUTHENTICATION -> {
                    viewModel.authenticate()
                    true
                }
                DEREGISTER -> {
                    true
                }
                ERROR -> {
                    false
                }
                DONE -> {
                    showDeregisterDialog()
                    false
                }
            }
        )
    }

    private fun showDeregisterDialog() {
        SimpleAlertDialogFragment.newInstance(
            titleId = R.string.deregister_dialog_title,
            message = getString(R.string.deregister_dialog_complete),
            positiveButtonTextId = R.string.button_done,
            listener = object: AlertDialogClickListener {
                override fun onPositiveClick(requestCode: Int) {
                    super.onPositiveClick(requestCode)
                    activity?.finish()
                }
            },
            isCancelable = false
        ).show(requireActivity().supportFragmentManager,
            SimpleAlertDialogFragment.TAG
        )
    }

    private fun showProgress(isShowing: Boolean = true) {
        binding.progressInclude.componentProgress.isVisible = isShowing
    }

    override fun onBiometricSuccess(pin: String) {
        viewModel.userPin = pin
    }

    override fun onBiometricFail(message: String) {
        viewModel.onBiometricFail(message)
    }

    override fun onBiometricCanceled(msg: String, errorCode: Int) {
        viewModel.startPinEntry()
        if (!viewModel.hasShownBiometricOption)
            showBiometricOption()
    }

    private fun showPinInput() {
        binding.dashboardGroup.isVisible = false

        showPinInput(binding.pinViewInclude) {
            viewModel.userPin = it
        }
    }

    private fun showBiometricOption() {
        viewModel.hasShownBiometricOption = true
        showBiometricOption(binding.pinViewInclude) {
            viewModel.startBiometric()
        }
    }


}