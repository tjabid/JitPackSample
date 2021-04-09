package tg.sdk.sca.presentation.ui.register

import android.app.Activity
import android.app.Activity.RESULT_FIRST_USER
import android.content.Intent
import android.view.KeyEvent
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.ERROR_NEGATIVE_BUTTON
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.data.biometric.BiometricPromptUtils
import tg.sdk.sca.data.biometric.BiometricPromptUtils.BIOMETRIC_SKIP_DIALOG_TAG
import tg.sdk.sca.data.biometric.BiometricPromptUtils.ERROR_AUTH_FAILED
import tg.sdk.sca.data.biometric.CryptographyManager
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.databinding.FragmentRegistrationBinding
import tg.sdk.sca.presentation.core.ui.BaseFragment
import tg.sdk.sca.presentation.ui.register.RegistrationViewModel.RegistrationViewState
import tg.sdk.sca.presentation.ui.register.RegistrationViewModel.RegistrationViewState.*
import tg.sdk.sca.presentation.utils.AlertDialogClickListener
import tg.sdk.sca.presentation.utils.SdkConstants
import tg.sdk.sca.presentation.utils.SdkConstants.PIN_LENGTH
import tg.sdk.sca.presentation.utils.SimpleAlertDialogFragment
import tg.sdk.sca.presentation.utils.extensions.ui.addAfterTextChangedListener
import tg.sdk.sca.presentation.utils.showGoToSettingsDialog
import timber.log.Timber
import java.security.KeyStoreException

class RegistrationFragment: BaseFragment() {

    override val layout: Int = R.layout.fragment_registration

    private lateinit var binding: FragmentRegistrationBinding
    override lateinit var viewModel: RegistrationViewModel

    private val cryptographyManager: CryptographyManager by lazy { CryptographyManager(requireContext()) }

    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentRegistrationBinding
    }

    override fun setupViews() {

        viewModelFactory = RegistrationViewModelFactory()
        viewModel = ViewModelProvider(this, viewModelFactory).get(RegistrationViewModel::class.java)


        view?.let {  view ->
            view.isFocusableInTouchMode = true
            view.requestFocus()
            view.setOnKeyListener { _, keyCode, _ ->
                if (keyCode == KeyEvent.KEYCODE_BACK) {
                    requireActivity().setResult(if (viewModel.viewState.value == COMPLETION) {
                        Activity.RESULT_OK
                    } else {
                        Activity.RESULT_CANCELED
                    })
                }
                false
            }
        }

        setupToolbar(binding.toolbarEnrollment, getString(R.string.registration_pin_title))
        viewModel.fetchPushToken(requireContext())

        viewModel.viewState.observe(this) {
            it?.let { handleViewState(it) }
        }
        viewModel.error.observe(this) {
            it?.let { handleCommonNetworkErrors(it) }
        }

        checkBiometricAuthenticate()
    }

    private fun handleViewState(state: RegistrationViewState) {
        showProgress(when (state) {
            PROGRESS -> true
            PIN_ENTRY -> {
                showPinInput()
                false
            }
            BIOMETRIC -> {
                showBiometricPromptForEncryption()
                true
            }
            INITIATE_TOKEN -> {
                binding.pinView.isVisible = false
                viewModel.fetchActivationToken()
                true
            }
            REGISTRATION -> {
                startRegistration()
                true
            }
            SIGN_REGISTRATION -> {
                true
            }
            ERROR -> {
                viewModel.error.value?.message?.let {
                    showMessage(
                        if (viewModel.error.value is NetworkError.Unknown) {
                            it
                        } else {
                            getString(R.string.registration_failure)
                        }
                    )
                }
                false
            }
            COMPLETION -> {
                onRegistrationComplete()
                false
            }
        })
    }

    private fun checkBiometricAuthenticate(fallback: (() -> Unit)? = null) {

        when (val code = BiometricPromptUtils.canAuthenticate(requireContext())) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                viewModel.initiateToken()
                onBiometricSuccess()
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                if (fallback != null) {
                    fallback.invoke()
                } else {
                    enrollBiometric()
                    showProgress()
                }
            }
            else -> {
                if (fallback != null) {
                    fallback.invoke()
                } else {
                    viewModel.onCheckBiometricFail(
                        BiometricPromptUtils.getAuthenticationErrorMessage(
                            requireContext(),
                            code
                        ),
                        requireContext()
                    )
                }
            }
        }
    }

    private fun startRegistration() {
        viewModel.registerUser()
    }

    private fun showPinInput() {

        binding.pinView.isVisible = true
        binding.messageGroup.isVisible = false

        binding.pin.addAfterTextChangedListener {
            it?.let {
                hideInputError()
            }
        }

        binding.pinConfirm.addAfterTextChangedListener {
            it?.let {
                hideInputError()
            }
        }

        binding.btnContinue.setOnClickListener {

            hideKeyboard()

            if (viewModel.isValidPin(binding.pin.text, binding.pinConfirm.text)) {
                binding.pin.text.toString().let { pin ->
                    viewModel.userPin = pin
                }
            } else {
                val pin = binding.pin.text
                val pinConfirm = binding.pinConfirm.text

                binding.pin.setText("")
                binding.pinConfirm.setText("")

                when {
                    pin == null -> binding.pinLayout.error = getString(R.string.invalid_pin)
                    pinConfirm == null -> {
                        binding.pinConfirmLayout.error = getString(R.string.invalid_pin)
                    }
                    pin.length != PIN_LENGTH -> {
                        binding.pinLayout.error = getString(R.string.error_message_pin_to_short)
                    }
                    pinConfirm.length != PIN_LENGTH -> {
                        binding.pinConfirmLayout.error = getString(R.string.error_message_pin_to_short)
                    }
                    pin != pinConfirm -> {
                        getString(R.string.mismatch_pin).let {
                            binding.pinLayout.error = it
                            binding.pinConfirmLayout.error = it
                        }
                    }
                    else -> {
                        getString(R.string.invalid_pin).let {
                            binding.pinLayout.error = it
                            binding.pinConfirmLayout.error = it
                        }
                    }
                }
            }
        }
    }

    private fun hideInputError() {
        if (binding.pinLayout.error != null) {
            binding.pinLayout.error = null
        }
        if (binding.pinConfirmLayout.error != null) {
            binding.pinConfirmLayout.error = null
        }
    }

    private fun showBiometricPromptForEncryption() {

        binding.pinView.isVisible = false

        if (BiometricPromptUtils.canAuthenticate(requireContext()) == BiometricManager.BIOMETRIC_SUCCESS) {

            val cipher = cryptographyManager.getInitializedCipherForEncryption {
                viewModel.onBiometricFail(it, requireContext())
            }

            cipher?.let { cipherObj ->
                val biometricPrompt =
                    BiometricPromptUtils.createBiometricPrompt(requireActivity(),
                        processSuccess = {
                            encryptAndStoreServerToken(it)
                        },
                        processFailure = { msg: String, errorCode: Int ->
                            handleBiometricError(msg, errorCode)
                        }
                    )

                val promptInfo = BiometricPromptUtils.createPromptInfo(
                    requireContext(),
                    getString(R.string.button_skip)
                )
                biometricPrompt.authenticate(
                    promptInfo,
                    BiometricPrompt.CryptoObject(cipherObj)
                )
            }
        }
    }

    private fun handleBiometricError(msg: String, errorCode: Int) {
        //todo - enhancement for LOCKOUT
        if (errorCode == ERROR_NEGATIVE_BUTTON && !viewModel.hasShownSkipBiometricDialog) {
            showSkipBiometricDialog()
        } else {
            viewModel.onBiometricFail(
                when (errorCode) {
                    ERROR_AUTH_FAILED -> getString(R.string.biometric_error_unknown)
                    else -> msg
                }, requireContext()
            )
        }
    }

    private fun encryptAndStoreServerToken(
        authResult: BiometricPrompt.AuthenticationResult
    ) {
        activity?.runOnUiThread { //todo check if need extension or alternative
            authResult.cryptoObject?.cipher?.apply {
                try {
                    viewModel.userPin?.let { token ->
                        cryptographyManager.persistCiphertextWrapperToSharedPrefs(
                            token = token,
                            cipher = this,
                            context = requireContext()
                        )
                    }

                    viewModel.setRegistration()

                } catch (e: KeyStoreException) {
                    //todo verify if we want redo enrollment on this
                    Timber.e(e)
                    viewModel.onBiometricFail(
                        getString(R.string.enroll_invalid_keystore),
                        requireContext()
                    )
                }
            } ?: viewModel.onBiometricFail(
                getString(R.string.enroll_invalid_keystore),
                requireContext()
            )
        }
    }

    private fun showSkipBiometricDialog() {
//        commented because we want to enforce biometrics
//        viewModel.hasShownSkipBiometricDialog = true

        SimpleAlertDialogFragment.newInstance(
            titleId = R.string.biometric_skip_dialog_title,
            messageId = R.string.biometric_skip_dialog_subtitle,
            positiveButtonTextId = R.string.biometric_skip_dialog_button,
//            negativeButtonTextId = R.string.button_skip, //commented because we want to enforce biometrics
            listener = object: AlertDialogClickListener{
                override fun onPositiveClick(requestCode: Int) {
                    super.onPositiveClick(requestCode)

                    showBiometricPromptForEncryption()
                }

                override fun onNegativeClick(requestCode: Int) {
                    super.onNegativeClick(requestCode)
                    viewModel.onBiometricFail("", requireContext())
                }
            },
            isCancelable = true
        ).show(requireActivity().supportFragmentManager, BIOMETRIC_SKIP_DIALOG_TAG)
    }

    private fun onRegistrationComplete() {

        showSuccessMessage(getString(R.string.registered_successfully))
        showMessage(getString(R.string.registered_successfully))
    }

    private fun showMessage(message: String) {

        binding.messageGroup.isVisible = true
        binding.pinView.isVisible = false

        binding.messageLabel.text = message
    }

    private fun showProgress(isShowing: Boolean = true) {
        binding.progress.isVisible = isShowing
    }

    private fun enrollBiometric() {
        showGoToSettingsDialog(requireActivity(), object : AlertDialogClickListener {
            override fun onPositiveClick(requestCode: Int) {
                super.onPositiveClick(requestCode)

                startEnrollment()
            }

//            commented because we want to enforce biometrics
//            override fun onNegativeClick(requestCode: Int) {
//                super.onNegativeClick(requestCode)
//
//                viewModel.onBiometricFail(
//                    "Biometric enrollment is not completed, Continue with PIN",
//                    requireContext()
//                )
//                showOptionEnrollBiometric()
//            }
        })
    }

    private fun startEnrollment() {

//                goToSecuritySettings()
//                startActivityForResult(Intent(Settings.ACTION_SECURITY_SETTINGS),
//                    BiometricPromptUtils.REQUEST_CODE_BIOMETRIC_ENROLL
//                )

        // Prompts the user to create credentials that your app accepts.
        // todo try to update once kotlin version is updated to latest: Miracl SDK dependent
        val enrollIntent =
            Intent("android.settings.BIOMETRIC_ENROLL"/*Settings.ACTION_BIOMETRIC_ENROLL*/).apply {
                putExtra(
                    "android.provider.extra.BIOMETRIC_AUTHENTICATORS_ALLOWED",//Settings.EXTRA_BIOMETRIC_AUTHENTICATORS_ALLOWED,
                    BiometricPromptUtils.getAuthenticators()
                )
            }
        startActivityForResult(enrollIntent, BiometricPromptUtils.REQUEST_CODE_BIOMETRIC_ENROLL)
    }

    private fun onBiometricSuccess() {
        binding.btnBiometricEnroll.isVisible = false
        viewModel.onBiometricSuccess(requireContext())
    }

    private fun showOptionEnrollBiometric() {
        binding.btnBiometricEnroll.isVisible = true
        binding.btnBiometricEnroll.setOnClickListener {
            startEnrollment()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == BiometricPromptUtils.REQUEST_CODE_BIOMETRIC_ENROLL) {
            checkBiometricAuthenticate {
                showOptionEnrollBiometric()
                viewModel.onCheckBiometricFail(
                    getString(R.string.biometric_error_not_setup),
                    requireContext()
                )
            }
        }
        super.onActivityResult(requestCode, resultCode, data)
    }
}