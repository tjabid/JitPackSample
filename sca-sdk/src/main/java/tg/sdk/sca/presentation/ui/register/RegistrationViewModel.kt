package tg.sdk.sca.presentation.ui.register

import android.content.Context
import android.text.Editable
import androidx.biometric.BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.messaging.FirebaseMessaging
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.AuthenticationUser
import kotlinx.coroutines.launch
import tg.sdk.sca.BuildConfig
import tg.sdk.sca.R
import tg.sdk.sca.data.biometric.BiometricPromptUtils
import tg.sdk.sca.data.common.network.BaseError
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.registration.model.ActivationToken
import tg.sdk.sca.domain.enrollment.register.InitiateUseCase
import tg.sdk.sca.domain.enrollment.register.MiraclRegistrationUseCase
import tg.sdk.sca.presentation.core.viewmodel.BaseViewModel
import tg.sdk.sca.presentation.utils.SdkConstants
import tg.sdk.sca.presentation.utils.SdkConstants.KEY_PREF_BIOMETRIC_SKIP
import tg.sdk.sca.presentation.utils.SdkConstants.SCA_SDK_SHARED_PREFS_FILENAME
import tg.sdk.sca.tgbobf.TgBobfSdk

class RegistrationViewModel: BaseViewModel() {

    enum class RegistrationViewState {
        PROGRESS,
        PIN_ENTRY,
        BIOMETRIC,
        INITIATE_TOKEN,
        REGISTRATION,
        SIGN_REGISTRATION,
        ERROR,
        COMPLETION
    }

    var viewState: MutableLiveData<RegistrationViewState> = MutableLiveData(RegistrationViewState.PROGRESS)
    var error: MutableLiveData<BaseError> = MutableLiveData()

    private var registrationUseCase: MiraclRegistrationUseCase = MiraclRegistrationUseCase()
    private var activationToken: ActivationToken? = null

    var hasShownSkipBiometricDialog: Boolean = false
    var userIdCandidate: String? = null
    var pushToken: String? = null

    var userPin: String? = null
        set(value) {
            field = value
            viewState.postValue(
                if (skipBiometric == true) {
                    RegistrationViewState.REGISTRATION
                } else {
                    RegistrationViewState.BIOMETRIC
                }
            )
        }

    init {
        skipBiometric = false
    }

    fun fetchPushToken(context: Context) {

        pushToken = context.getSharedPreferences(
            SCA_SDK_SHARED_PREFS_FILENAME,
            Context.MODE_PRIVATE
        ).getString(
            SdkConstants.KEY_PUSH_TOKEN, ""
        )

        if (pushToken.isNullOrEmpty()) {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
                if (!task.isSuccessful) {
                    return@OnCompleteListener
                }
                // Get new FCM registration token
                pushToken = task.result
            })
        }
    }

    fun fetchActivationToken() {

        viewModelScope.launch(IO) {
            InitiateUseCase().execute { result ->
                handleResult(
                    result,
                    {
                        activationToken = it
                        userIdCandidate = it?.userId
                        viewState.postValue(RegistrationViewState.PIN_ENTRY)
                    },
                    {
                        error.postValue(it)
                        viewState.postValue(RegistrationViewState.ERROR)
                    }
                )
            }
        }
    }

    fun registerUser() {

        if (userIdCandidate.isNullOrEmpty() || activationToken == null || activationToken?.actToken.isNullOrEmpty()) {
            error.postValue(NetworkError.Unknown("Failed to retrieve valid values, Please try again"))
            viewState.postValue(RegistrationViewState.ERROR)
            return
        }

        val token = activationToken?.let {
            com.miracl.trust.registration.ActivationToken(it.actToken, it.expireTime)
        } ?: com.miracl.trust.registration.ActivationToken("", 0)

        val pinProvider = PinProvider {
            it.consume(userPin)
        }

        viewModelScope.launch(IO) {
            registrationUseCase.execute(
                userId = userIdCandidate!!,
                activationToken = token,
                pushToken = pushToken,
                pinProvider = pinProvider
            ) { result ->
                when (result) {
                    is MiraclSuccess -> {
                        viewState.postValue(RegistrationViewState.SIGN_REGISTRATION)
                        registerSign(result.value)
                    }
                    is MiraclError -> {
                        //todo check for already registered users
                        error.postValue(NetworkError.Unknown(result.value.message ?: "Failed to register!"))
                        viewState.postValue(RegistrationViewState.ERROR)
                    }
                }
            }
        }
    }

    private fun registerSign(authenticationUser: AuthenticationUser?) {
        if (userIdCandidate.isNullOrEmpty() || authenticationUser == null) {
            error.postValue(NetworkError.Unknown("Failed to retrieve valid values, please try again"))
            viewState.postValue(RegistrationViewState.ERROR)
            return
        }

        val authenticationPinProvider = PinProvider {
            it.consume(userPin)
        }

        val signPinProvider = PinProvider {
            it.consume(userPin)
        }

        viewModelScope.launch(IO) {
            registrationUseCase.executeSignUser(
                authenticationUser = authenticationUser,
                accessId = "123",//todo verify in new Miracl SDK
                authenticationPinProvider = authenticationPinProvider,
                signingPinProvider = signPinProvider,
            ) { result ->
                when (result) {
                    is MiraclSuccess -> {
                        TgBobfSdk.userId = userIdCandidate
                        viewState.postValue(RegistrationViewState.COMPLETION)
                    }
                    is MiraclError -> {
                        error.postValue(NetworkError.Unknown(result.value.message ?: "Failed to register!"))
                        viewState.postValue(RegistrationViewState.ERROR)
                    }
                }
            }
        }
    }

    fun isValidPin(pin: Editable?, pinConfirm: Editable?): Boolean {
        return !pin.isNullOrEmpty() && !pinConfirm.isNullOrEmpty()
                && pin.length == SdkConstants.PIN_LENGTH
                && pin.toString() == pinConfirm.toString()
    }

    private fun saveBiometricPref(context: Context) {
        context.getSharedPreferences(SCA_SDK_SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
            .edit().putBoolean(KEY_PREF_BIOMETRIC_SKIP, skipBiometric!!).apply()
    }

    fun onBiometricSuccess(context: Context, skip: Boolean = false) {
        skipBiometric = skip
        saveBiometricPref(context) //todo remove context and move save pref to central
    }

    fun onBiometricFail(message: String, context: Context) {
        if (canFallbackToPinOnBiometricFailure(message, context)) {
            onBiometricSuccess(context, true)
            error.postValue(NetworkError.Unknown(message))
            if (userIdCandidate.isNullOrEmpty() || activationToken == null || activationToken?.actToken.isNullOrEmpty()) {
                initiateToken()
            } else {
                viewState.postValue(RegistrationViewState.PIN_ENTRY)
            }
        }
    }

    fun onCheckBiometricFail(message: String, context: Context) {
        if (canFallbackToPinOnBiometricFailure(message, context)) {
            onBiometricSuccess(context, true)
            error.postValue(NetworkError.Unknown(message))
            viewState.postValue(RegistrationViewState.INITIATE_TOKEN)
        }
    }

    private fun canFallbackToPinOnBiometricFailure(message: String, context: Context): Boolean {
        if (BuildConfig.FLAVOR == SdkConstants.BuildFlavour.KFH &&
            BiometricPromptUtils.canAuthenticate(context) != BIOMETRIC_ERROR_NO_HARDWARE) {
            error.postValue(
                NetworkError.Unknown(
                    context.getString(
                        R.string.biometric_error_cannot_go_ahead,
                        message
                    )
                )
            )
            viewState.postValue(RegistrationViewState.ERROR)
            return false
        }
        return true
    }

    fun initiateToken() {
        viewState.postValue(RegistrationViewState.INITIATE_TOKEN)
    }

    fun setRegistration() {
        viewState.postValue(RegistrationViewState.REGISTRATION)
    }
}

class RegistrationViewModelFactory : ViewModelProvider.Factory {

    override fun <T : ViewModel?> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegistrationViewModel::class.java)) {
            return RegistrationViewModel() as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}