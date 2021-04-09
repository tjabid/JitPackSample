package tg.sdk.sca.presentation.ui.dashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.MiraclTrust
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.SigningUser
import com.miracl.trust.signing.Signature
import kotlinx.coroutines.launch
import tg.sdk.sca.data.common.network.BaseError
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.consent.DeregisterRequest
import tg.sdk.sca.data.consent.Header
import tg.sdk.sca.domain.enrollment.register.DeregisterUseCase
import tg.sdk.sca.presentation.core.viewmodel.AuthenticationBaseViewModel
import tg.sdk.sca.presentation.ui.dashboard.DashboardViewModel.DashboardViewState.*
import tg.sdk.sca.tgbobf.TgBobfSdk
import timber.log.Timber
import java.util.*

class DashboardViewModel: AuthenticationBaseViewModel() {

    enum class DashboardViewState {
        INITIAL_VIEW,
        BIOMETRIC,
        PIN_ENTRY,
        AUTHENTICATION,
        ERROR,
        DEREGISTER,
        DONE
    }

    var viewState: MutableLiveData<DashboardViewState> = MutableLiveData(INITIAL_VIEW)
    var error: MutableLiveData<BaseError> = MutableLiveData()

    private lateinit var getConsentDate: Date
    private var authenticating: Boolean = false

    var userPin: String? = null
        set(value) {
            field = value
            if (viewState.value != AUTHENTICATION) {
                viewState.postValue(AUTHENTICATION)
            }
        }

    fun startPinEntry() {
        viewState.postValue(PIN_ENTRY)
    }

    fun onBiometricFail(message: String) {
        error.postValue(NetworkError.Unknown(message))
        viewState.postValue(PIN_ENTRY)
    }

    fun startBiometric() {
        viewState.postValue(BIOMETRIC)
    }

    fun authenticate() {

        if (authenticating) {
            Timber.d("already authenticating $authenticating")
            return
        }

        if (signingUser == null) {
            error.postValue(NetworkError.Unauthorized)
            viewState.postValue(ERROR)

            return
        }

        val pinProvider = PinProvider {
            it.consume(userPin)
        }

        val mpinId = getMPin()
        val message = getHash("action=DEREGISTER|mpinId=$mpinId")
        if (message == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(ERROR)
            return
        }
        getConsentDate = Date()

        authenticating = true
        getSignature(
            message = message,
            signingUser = signingUser!!,
            date = getConsentDate,
            pinProvider = pinProvider
        ) { result ->
            authenticating = false
            when (result) {
                is MiraclSuccess -> {
                    deregister(result.value, mpinId)
                }
                is MiraclError -> {
                    error.postValue(NetworkError.Authentication)
                    viewState.postValue(ERROR)
                }
            }
        }
    }

    private fun deregister(signature: Signature, mPin: String) {

        viewState.postValue(DEREGISTER)

        val deregisterRequest = DeregisterRequest(
            mpinId = mPin,
            header = Header(
                signature = signature,
                timestamp = getTimeStamp(getConsentDate)
            )
        )

        viewModelScope.launch(IO) {
            DeregisterUseCase().execute(deregisterRequest) { result ->
                handleResult(
                    result,
                    {
                        deleteMiraclLocalUser(signingUser!!)
                        viewState.postValue(DONE)
                    },
                    {
                        error.postValue(it)
                        viewState.postValue(ERROR)
                    }
                )
            }
        }
    }

    private fun deleteMiraclLocalUser(signingUser: SigningUser) {
        MiraclTrust.getInstance().deleteSigningUser(signingUser)
        MiraclTrust.getInstance().authenticationUsers
            .find { user -> user.identity.userId == TgBobfSdk.userId }?.let {
                MiraclTrust.getInstance().deleteUser(it)
            }
        TgBobfSdk.userId = null
    }

}

