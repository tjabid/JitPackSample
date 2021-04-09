package tg.sdk.sca.presentation.core.ui

import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.view.isVisible
import tg.sdk.sca.R
import tg.sdk.sca.data.biometric.BiometricPromptUtils
import tg.sdk.sca.data.biometric.CryptographyManager
import tg.sdk.sca.databinding.ComponentAuthPinViewBinding
import tg.sdk.sca.presentation.utils.extensions.ui.addAfterTextChangedListener
import timber.log.Timber

abstract class AuthenticationBaseFragment: BaseFragment() {

    private lateinit var biometricPrompt: BiometricPrompt
    private val cryptographyManager: CryptographyManager by lazy { CryptographyManager(requireContext()) }
    var biometricUnsupported: Boolean = false

    fun canAuthenticateWithBiometric(): Boolean {

        if (viewModel?.isEnrolledUsingPin(requireContext()) == true) {
            biometricUnsupported = true
            return false
        }

        biometricUnsupported = when (BiometricPromptUtils.canAuthenticate(requireContext())) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                false
            }
            else -> {
                true
            }
        }
        return !biometricUnsupported
    }

    fun showBiometricPromptForDecryption() {

        try {
            val cipher = cryptographyManager.getInitializedCipherForDecryption {
                onBiometricFail(it)
            }
            cipher?.let {
                biometricPrompt = BiometricPromptUtils.createBiometricPrompt(
                    requireActivity(),
                    processSuccess = {
                        decryptServerTokenFromStorage(it)
                    },
                    processFailure = { msg: String, errorCode: Int ->
                        handleBiometricError(msg, errorCode)
                    }
                )
                val promptInfo = BiometricPromptUtils.createPromptInfo(
                    requireContext(),
                    getString(R.string.biometric_dialog_use_alternative)
                )
                biometricPrompt.authenticate(promptInfo, BiometricPrompt.CryptoObject(cipher))
            }
        } catch (e: Exception) {
            Timber.e(e)
            onBiometricFail(getString(R.string.biometric_error_unknown))
        }
    }

    private fun decryptServerTokenFromStorage(authResult: BiometricPrompt.AuthenticationResult) {
        try {
            authResult.cryptoObject?.cipher?.let {
                val userPin = cryptographyManager.decryptData(it)
                if (userPin.isEmpty()) {
                    onBiometricFail(getString(R.string.biometric_error_unknown))
                } else {
                    onBiometricSuccess(userPin)
                }
            }
        } catch (e: Exception) {
            //todo verify if we want redo enrollment on this
            Timber.e(e)
            onBiometricFail(getString(R.string.biometric_error_unknown))
        }
    }

    private fun handleBiometricError(msg: String, errorCode: Int) {
        //todo - enhancement for LOCKOUT
        if (errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON || errorCode == BiometricPrompt.ERROR_USER_CANCELED) {
            onBiometricCanceled(msg, errorCode)
        } else {
            onBiometricFail(
                when (errorCode) {
                    BiometricPromptUtils.ERROR_AUTH_FAILED -> getString(R.string.biometric_error_unknown)
                    else -> msg
                }
            )
        }
    }

    open fun onBiometricSuccess(pin: String) {

    }

    open fun onBiometricFail(message: String) {
        showErrorMessage(message)
    }

    open fun onBiometricCanceled(msg: String, errorCode: Int) {

    }

    fun showPinInput(binding: ComponentAuthPinViewBinding, onValidPin: (String) -> Unit) {

        changePinViewVisibility(binding, true)

        binding.pin.addAfterTextChangedListener { it?.let { hideInputError(binding) } }

        binding.btnContinue.setOnClickListener {

            hideKeyboard()

            if (viewModel?.isValidPin(binding.pin.text) == true) {
                binding.pin.text.toString().let { pin ->
                    onValidPin.invoke(pin)
                    changePinViewVisibility(binding, false)
                }
            } else {
                binding.pin.setText("")
                binding.pinLayout.error = getString(R.string.invalid_pin)
            }
        }
    }

    private fun hideInputError(binding: ComponentAuthPinViewBinding) {
        if (binding.pinLayout.error != null) {
            binding.pinLayout.error = null
        }
    }

    fun changePinViewVisibility(binding: ComponentAuthPinViewBinding, isVisible: Boolean) {
        binding.pinView.isVisible = isVisible
    }

    fun showBiometricOption(binding: ComponentAuthPinViewBinding, startBiometric: () -> Unit) {
        binding.btnBiometricEnroll.isVisible = true
        binding.btnBiometricEnroll.setOnClickListener {
            startBiometric.invoke()
        }
    }
}