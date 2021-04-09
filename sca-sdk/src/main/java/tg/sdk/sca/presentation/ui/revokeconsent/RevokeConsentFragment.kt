package tg.sdk.sca.presentation.ui.revokeconsent

import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.databinding.FragmentRevokeConsentBinding
import tg.sdk.sca.presentation.core.ui.AuthenticationBaseFragment
import tg.sdk.sca.presentation.core.viewmodel.TgBobfSdkViewModelFactory
import tg.sdk.sca.presentation.ui.authenticate.ConsentAccountAdapter
import tg.sdk.sca.presentation.ui.revokeconsent.RevokeConsentViewModel.RevokeConsentViewState.*
import tg.sdk.sca.presentation.utils.AlertDialogClickListener
import tg.sdk.sca.presentation.utils.SimpleAlertDialogFragment

class RevokeConsentFragment: AuthenticationBaseFragment() {

    override val layout: Int = R.layout.fragment_revoke_consent

    private lateinit var binding: FragmentRevokeConsentBinding
    override lateinit var viewModel: RevokeConsentViewModel

    private lateinit var accountAdapter: ConsentAccountAdapter

    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentRevokeConsentBinding
    }

    override fun setupViews() {

        viewModelFactory = TgBobfSdkViewModelFactory(RevokeConsentViewModel())
        viewModel = ViewModelProvider(this, viewModelFactory).get(RevokeConsentViewModel::class.java)

        viewModel.tgBobfConsent = arguments?.let {
            val safeArgs = RevokeConsentFragmentArgs.fromBundle(it)
            safeArgs.tgBobfConsent.let { consent ->
                consent
            }
        }

        setupToolbar(binding.toolbarRevokeConsent, getString(R.string.title_revoke_consent))

        viewModel.viewState.observe(this) {
            it?.let { handleViewState(it) }
        }
        viewModel.error.observe(this) {
            it?.let { handleCommonNetworkErrors(it) }
        }

        binding.consentLayout.btnApprove.text = getString(R.string.revoke)
        binding.consentLayout.btnDecline.isVisible = false
        binding.consentLayout.consentTitle.text = getString(R.string.revoke_consent_intro)

        binding.consentLayout.btnApprove.setOnClickListener {
            if (canAuthenticateWithBiometric()) {
                viewModel.startBiometric()
            } else {
                viewModel.startPinEntry()
            }
        }

        viewModel.tgBobfConsent?.let {

            binding.consentLayout.rvConsentAccounts.apply {
                accountAdapter = ConsentAccountAdapter(it.accounts, showSwitch = false)
                adapter = accountAdapter
            }

        }
    }

    private fun handleViewState(state: RevokeConsentViewModel.RevokeConsentViewState) {
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
                    startAuthentication()
                    true
                }
                ERROR -> {
                    false
                }
                SIGNED -> {
                    true
                }
                DONE -> {
                    showMessage(getString(R.string.end_msg_revoke))
                    false
                }
            }
        )
    }

    private fun showMessage(message: String) {

        SimpleAlertDialogFragment.newInstance(
            titleId = R.string.title_revoke_consent,
            message = message,
            positiveButtonTextId = R.string.button_done,
            listener = object: AlertDialogClickListener {
                override fun onPositiveClick(requestCode: Int) {
                    super.onPositiveClick(requestCode)
                    navController.navigate(RevokeConsentFragmentDirections.actionRevokeToDashboard())
                }
            },
            isCancelable = true
        ).show(requireActivity().supportFragmentManager,
            SimpleAlertDialogFragment.TAG
        )
    }

    private fun showProgress(isShowing: Boolean = true) {
        binding.progressInclude.componentProgress.isVisible = isShowing
    }

    private fun startAuthentication() {
        viewModel.authenticate()
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
        binding.consentLayout.componentAuthConsent.isVisible = false

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