package tg.sdk.sca.presentation.ui.manageconsent

import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.databinding.FragmentManageConsentBinding
import tg.sdk.sca.presentation.core.ui.AuthenticationBaseFragment
import tg.sdk.sca.presentation.core.viewmodel.TgBobfSdkViewModelFactory
import tg.sdk.sca.presentation.ui.manageconsent.ManageConsentViewModel.ManageConsentViewState.*

class ManageConsentFragment: AuthenticationBaseFragment() {

    override val layout: Int = R.layout.fragment_manage_consent

    private lateinit var binding: FragmentManageConsentBinding
    override lateinit var viewModel: ManageConsentViewModel

    private lateinit var consentAdapter: ConsentAdapter

    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentManageConsentBinding
    }

    override fun setupViews() {

        viewModelFactory = TgBobfSdkViewModelFactory(ManageConsentViewModel())
        viewModel =
            ViewModelProvider(this, viewModelFactory).get(ManageConsentViewModel::class.java)

        setupToolbar(binding.toolbarManageConsent, getString(R.string.title_manage_consent_ais))

        binding.rvConsent.apply {
            consentAdapter = ConsentAdapter(ArrayList(),
                object : ConsentAdapter.OnItemClickListener {
                    override fun onItemClick(item: TgBobfConsent) {
                        navController.navigate(
                            ManageConsentFragmentDirections.actionManageToRevokeConsent(
                                item
                            )
                        )
                    }
                })
            adapter = consentAdapter
        }

        viewModel.tgBobfConsent.observe(this) {
            it?.let { handleRetrieveConsent(it) }
        }
        viewModel.viewState.observe(this) {
            it?.let { handleViewState(it) }
        }
        viewModel.error.observe(this) {
            it?.let { handleCommonNetworkErrors(it) }
        }

        if (viewModel.tgBobfConsent.value == null) {
            if (canAuthenticateWithBiometric()) {
                viewModel.startBiometric()
            } else {
                viewModel.startPinEntry()
            }
        }
    }

    private fun handleViewState(state: ManageConsentViewModel.ManageConsentViewState) {
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
                    showScreenMessage()
                    false
                }
                SIGNED -> {
                    false
                }
                FETCH_CONSENT -> {
                    true
                }
                LOAD_CONSENT -> {
                    false
                }
                DONE -> {
                    false
                }
            }
        )
    }

    private fun handleRetrieveConsent(tgBobfConsentList: List<TgBobfConsent>) {
        changePinViewVisibility(binding.pinViewInclude, false)
        if (tgBobfConsentList.isEmpty()) {
            binding.consentNoItem.text = getString(R.string.manage_consent_no_item)
            binding.consentNoItem.isVisible = true
        } else {
            binding.manageConsentGroup.isVisible = true
        }
        consentAdapter.updateList(tgBobfConsentList)
    }

    private fun showProgress(isShowing: Boolean = true) {
        binding.progressInclude.componentProgress.isVisible = isShowing
    }

    private fun startAuthentication() {
        viewModel.authenticate()
    }

    private fun showScreenMessage() {
        binding.consentNoItem.text = getString(R.string.manage_consent_failed)
        binding.consentNoItem.isVisible = true
        binding.manageConsentGroup.isVisible = false
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
        binding.manageConsentGroup.isVisible = false

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