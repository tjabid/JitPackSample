package tg.sdk.sca.presentation.utils

import android.annotation.SuppressLint
import android.os.Handler
import android.os.Looper
import androidx.annotation.StringRes
import androidx.biometric.BiometricPrompt
import androidx.biometric.BiometricPrompt.PromptInfo
import androidx.fragment.app.FragmentActivity
import tg.sdk.sca.R
import timber.log.Timber
import java.util.concurrent.Executor

private const val BIOMETRIC_DIALOG_TAG = "GoToSettingsDialog"

/**
 * Authenticates the user with fingerprint and sends corresponding response back to Flutter.
 *
 *
 * One instance per call is generated to ensure readable separation of executable paths across
 * method calls.
 */
class AuthenticationHelper(
    private val activity: FragmentActivity,
    private val onSuccess: () -> Unit,
    private val onFailure: ((canceledByUser: Boolean) -> Unit)? = null,
    private val onError: ((String) -> Boolean)? = null
) : AlertDialogClickListener {

    private val promptInfo: PromptInfo
    private val uiThreadExecutor: UiThreadExecutor
    private var biometricPrompt: BiometricPrompt? = null

    /** Start the fingerprint listener.  */
    fun authenticate() {
        biometricPrompt = BiometricPrompt(activity, uiThreadExecutor, object : BiometricPrompt.AuthenticationCallback() {
            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                this@AuthenticationHelper.onAuthenticationSucceeded(result)
            }

            override fun onAuthenticationFailed() {
                this@AuthenticationHelper.onAuthenticationFailed()
            }

            override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                this@AuthenticationHelper.onAuthenticationError(errorCode)
            }
        })
        biometricPrompt!!.authenticate(promptInfo)
    }

    /** Cancels the fingerprint authentication.  */
    fun stopAuthentication() {
        biometricPrompt?.cancelAuthentication()
        biometricPrompt = null
    }

    @SuppressLint("SwitchIntDef")
    private fun onAuthenticationError(errorCode: Int) {
        Timber.d("BiometricHelper onAuthenticationError errorCode=$errorCode")
        when (errorCode) {
            BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> showGoToSettings(R.string.biometric_error_passcode_not_set)
            BiometricPrompt.ERROR_NO_SPACE, BiometricPrompt.ERROR_NO_BIOMETRICS -> showGoToSettings(R.string.biometric_error_not_enrolled)
            BiometricPrompt.ERROR_HW_UNAVAILABLE, BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                showBiometricError(R.string.biometric_error_not_available)
            BiometricPrompt.ERROR_LOCKOUT -> showBiometricError(R.string.biometric_error_locked_out)
            BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> showBiometricError(R.string.biometric_error_locked_out)
            BiometricPrompt.ERROR_USER_CANCELED -> onFailure?.invoke(true)
            else -> onFailure?.invoke(false)
        }
    }

    private fun showGoToSettings(@StringRes errorTextId: Int) {
        val needToShowDialog = onError?.invoke(activity.getString(errorTextId)) ?: true
        if (needToShowDialog) {
            showGoToSettingsDialog(activity, this)
        }
    }

    private fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
        Timber.d("BiometricHelper onAuthenticationSucceeded result=$result")
        onSuccess()
    }

    private fun onAuthenticationFailed() {
        Timber.d("BiometricHelper onAuthenticationFailed")
    }

    private fun showBiometricError(@StringRes messageId: Int) {
        val message = activity.getString(messageId)
        val needToShowDialog = onError?.invoke(message) ?: true
        if (needToShowDialog) {
            showErrorMessage(message)
        }
    }

    private fun showErrorMessage(
        message: String
    ) {
        SimpleAlertDialogFragment.newInstance(
            message = message,
            positiveButtonTextId = R.string.button_cancel,
            listener = this
        ).show(activity.supportFragmentManager, "BiometricErrorDialog")
    }

    override fun onNegativeClick(requestCode: Int) {
        onFailure?.invoke(true)
    }

    override fun onPositiveClick(requestCode: Int) {
        onFailure?.invoke(true)
        activity.goToSecuritySettings()
    }

    private class UiThreadExecutor : Executor {
        val handler = Handler(Looper.getMainLooper())
        override fun execute(command: Runnable) {
            handler.post(command)
        }
    }

    init {
        uiThreadExecutor = UiThreadExecutor()
        promptInfo = PromptInfo.Builder()
            .setTitle(activity.getString(R.string.biometric_dialog_title))
            .setSubtitle(activity.getString(R.string.biometric_dialog_subtitle))
            .setDeviceCredentialAllowed(true)
            .build()
    }
}

fun showGoToSettingsDialog(activity: FragmentActivity, listener: AlertDialogClickListener) {
    SimpleAlertDialogFragment.newInstance(
        titleId = R.string.biometric_go_to_settings_title,
        messageId = R.string.biometric_go_to_settings_description,
        positiveButtonTextId = R.string.go_to_settings,
//        negativeButtonTextId = R.string.button_cancel,//commented because we want to enforce biometrics
        listener = listener,
        isCancelable = false
    ).show(activity.supportFragmentManager, BIOMETRIC_DIALOG_TAG)
}