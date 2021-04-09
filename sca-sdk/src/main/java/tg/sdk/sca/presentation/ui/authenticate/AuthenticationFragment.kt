package tg.sdk.sca.presentation.ui.authenticate

import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.data.consent.TgAccount
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.databinding.FragmentAuthenticationBinding
import tg.sdk.sca.presentation.core.ui.AuthenticationBaseFragment
import tg.sdk.sca.presentation.core.viewmodel.TgBobfSdkViewModelFactory
import tg.sdk.sca.presentation.ui.authenticate.AuthenticationViewModel.AuthenticationViewState
import tg.sdk.sca.presentation.ui.authenticate.AuthenticationViewModel.AuthenticationViewState.*
import tg.sdk.sca.presentation.utils.SdkConstants.CONSENT_TYPE_PIS_DOMESTIC

class AuthenticationFragment: AuthenticationBaseFragment() {

    override val layout: Int = R.layout.fragment_authentication

    private lateinit var binding: FragmentAuthenticationBinding
    override lateinit var viewModel: AuthenticationViewModel

    private lateinit var accountAdapter: ConsentAccountAdapter

    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentAuthenticationBinding
    }

    override fun setupViews() {

        viewModelFactory = TgBobfSdkViewModelFactory(AuthenticationViewModel())
        viewModel = ViewModelProvider(this, viewModelFactory).get(AuthenticationViewModel::class.java)

        arguments?.let {
            val safeArgs = AuthenticationFragmentArgs.fromBundle(it)
            safeArgs.sessionId?.let {
                viewModel.documentId = safeArgs.sessionId
            }
        }

        setupToolbar(binding.toolbarAuth, getString(R.string.title_consent_authorize))

        viewModel.tgBobfConsent.observe(this) {
            it?.let { handleRetrieveConsent(it) }
        }
        viewModel.viewState.observe(this) {
            it?.let { handleViewState(it) }
        }
        viewModel.error.observe(this) {
            it?.let { handleCommonNetworkErrors(it) }
        }

        binding.authGroup.isVisible = true
        binding.btnAuthenticate.setOnClickListener {
            if (canAuthenticateWithBiometric()) {
                viewModel.startBiometric()
            } else {
                viewModel.startPinEntry()
            }
        }


        binding.consentLayout.rvConsentAccounts.apply {
            accountAdapter = ConsentAccountAdapter(
                ArrayList(),
                checkChangeListener = object : ConsentAccountAdapter.OnItemCheckChangeListener {
                    override fun onChange() {
                        handleApproveButton()
                    }
                }
            )
            adapter = accountAdapter
        }
    }

    private fun handleRetrieveConsent(tgBobfConsent: TgBobfConsent) {

        tgBobfConsent.consentType?.let {
            binding.toolbarAuth.tgToolbarBackTitle.text = getString(
                if (it == CONSENT_TYPE_PIS_DOMESTIC) {
                    binding.consentLayout.apply {
                        tgBobfConsent.consent.initiation?.let { initiation ->
                            pispName.text = initiation.CreditorAccount.Name
                            pispIdentity.text = initiation.CreditorAccount.Identification
                            "${initiation.InstructedAmount.Currency} ${initiation.InstructedAmount.Amount}".also { amount ->
                                pispAmount.text = amount
                            }
                        }
                        pisDetailView.isVisible = true
                    }
                    accountAdapter.setMultiSelectionEnable(false)

                    R.string.title_consent_authorize_pis
                } else {
                    R.string.title_consent_authorize_ais
                }
            )
        }
        tgBobfConsent.tppName?.let { binding.consentLayout.consentTpp.text = it }

        accountAdapter.updateList(tgBobfConsent.accounts)

//        binding.consentLayout.tvPermissions.text = consentResponse.consent.permissions.joinToString(separator = ", ") { it }

        binding.consentLayout.btnApprove.setOnClickListener {
            onConsentApproved()
        }
        binding.consentLayout.btnDecline.setOnClickListener {
            updateConsent(approved = false, arrayListOf())
        }

        handleApproveButton()
    }

    private fun onConsentApproved() {
        if (accountAdapter.selectedAccounts.isEmpty()) {
            if (!viewModel.isApprovedDisabled()) {
                showErrorMessage(getString(R.string.error_no_account_selected))
            }
        } else {
            updateConsent(approved = true, accountAdapter.selectedAccounts)
        }
    }

    private fun handleApproveButton() {
        if (viewModel.isApprovedDisabled()) {
            binding.consentLayout.btnApprove.backgroundTintList =
                if (accountAdapter.selectedAccounts.isEmpty()) {
                    ContextCompat.getColorStateList(requireContext(), R.color.grey)
                } else {
                    null
                }
        }
    }

    private fun updateConsent(approved: Boolean, accounts: MutableList<String>) {
        viewModel.updateConsentAuthenticate(approved, accounts)
    }

    private fun handleViewState(state: AuthenticationViewState) {
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
                    false
                }
                ERROR -> {
                    viewModel.error.value?.message?.let {
                        showMessage(it)
                    }
                    false
                }
                SIGNED -> {//todo check if we need this
                    onSigned()
                    false
                }
                FETCH_CONSENT -> {
                    //todo
//                    binding.authTitle.setText(viewModel.hash)
                    true
                }
                LOAD_CONSENT -> {
                    showConsent()
                    false
                }
                AUTHENTICATION_UPDATE -> true
                UPDATE_CONSENT -> {
                    binding.consentLayout.componentAuthConsent.isVisible = false
                    true
                }
                DONE -> {
                    onSigned()
                    false
                }
            }
        )
    }

    private fun showConsent() {
        binding.consentLayout.componentAuthConsent.isVisible = true
        binding.authGroup.isVisible = false
        changePinViewVisibility(binding.pinViewInclude, false)
    }

    private fun onSigned() {
        binding.consentLayout.componentAuthConsent.isVisible = false
        binding.authGroup.isVisible = false
        changePinViewVisibility(binding.pinViewInclude, false)

        binding.message.text = getString(R.string.auth_msg_success)
        binding.ivMessage.isVisible = true
        binding.successView.isVisible = true
    }

    private fun showMessage(message: String) {
        binding.consentLayout.componentAuthConsent.isVisible = false
        binding.authGroup.isVisible = false
        changePinViewVisibility(binding.pinViewInclude, false)

        binding.message.text = message
        binding.ivMessage.isVisible = false
        binding.successView.isVisible = true
    }

    private fun showProgress(isShowing: Boolean = true) {
        binding.progressInclude.componentProgress.isVisible = isShowing
    }

    private fun startAuthentication() {
        viewModel.authenticate()
    }

    private fun showPinInput() {

        binding.authGroup.isVisible = false
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
}