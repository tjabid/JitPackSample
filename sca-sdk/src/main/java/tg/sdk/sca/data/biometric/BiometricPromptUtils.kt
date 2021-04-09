package tg.sdk.sca.data.biometric

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import tg.sdk.sca.R
import timber.log.Timber

object BiometricPromptUtils {

    const val SHARED_PREFS_FILENAME = "biometric_prefs"
    const val CIPHERTEXT_WRAPPER = "ciphertext_wrapper"

    const val ERROR_AUTH_FAILED = -1

    const val REQUEST_CODE_BIOMETRIC_ENROLL = 10101
    const val BIOMETRIC_SKIP_DIALOG_TAG = "SkipBiometricsDialog"

    fun createBiometricPrompt(
        activity: FragmentActivity,
        processSuccess: (BiometricPrompt.AuthenticationResult) -> Unit,
        processFailure: (msg: String, errorCode: Int) -> Unit
    ): BiometricPrompt {
        val executor = ContextCompat.getMainExecutor(activity)

        val callback = object : BiometricPrompt.AuthenticationCallback() {

            override fun onAuthenticationError(errCode: Int, errString: CharSequence) {
                super.onAuthenticationError(errCode, errString)
                Timber.d("errCode is $errCode and errString is: $errString")
                processFailure(getAuthenticationErrorMessage(activity, errCode), errCode)
            }

            override fun onAuthenticationFailed() {
                super.onAuthenticationFailed()
                Timber.d("User biometric rejected.")
//                processFailure("", -1)
            }

            override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                super.onAuthenticationSucceeded(result)
                Timber.d("Authentication was successful")
                processSuccess(result)
            }
        }
        return BiometricPrompt(activity, executor, callback)
    }

    fun createPromptInfo(context: Context, alternativeText: String): BiometricPrompt.PromptInfo =
        BiometricPrompt.PromptInfo.Builder().apply {
            setTitle(context.getString(R.string.biometric_dialog_title))
            setSubtitle(context.getString(R.string.biometric_dialog_subtitle))
            setConfirmationRequired(false)
            setNegativeButtonText(alternativeText)
//            setAllowedAuthenticators(getAuthenticators())
        }.build()


    fun getAuthenticators(): Int = BIOMETRIC_STRONG //or DEVICE_CREDENTIAL //or BIOMETRIC_WEAK

    fun canAuthenticate(context: Context): Int {
        return BiometricManager.from(context).canAuthenticate(getAuthenticators())
    }

    fun getAuthenticationErrorMessage(context: Context, errorCode: Int): String {
        return context.getString(
            when (errorCode) {
                BiometricPrompt.ERROR_NO_DEVICE_CREDENTIAL -> R.string.biometric_error_passcode_not_set
                BiometricPrompt.ERROR_NO_SPACE, BiometricPrompt.ERROR_NO_BIOMETRICS -> R.string.biometric_error_not_enrolled
                BiometricPrompt.ERROR_HW_UNAVAILABLE, BiometricPrompt.ERROR_HW_NOT_PRESENT ->
                    R.string.biometric_error_not_available
                BiometricPrompt.ERROR_LOCKOUT -> R.string.biometric_error_locked_out
                BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> R.string.biometric_error_locked_out_permanent
                else -> R.string.biometric_error_unknown
            }
        )
    }
}