package tg.sdk.sca.presentation.ui.revokeconsent

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.signing.Signature
import kotlinx.coroutines.launch
import tg.sdk.sca.data.common.network.BaseError
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.consent.Header
import tg.sdk.sca.data.consent.RevokeConsentRequest
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.domain.enrollment.register.RevokeConsentUseCase
import tg.sdk.sca.presentation.core.viewmodel.AuthenticationBaseViewModel
import tg.sdk.sca.presentation.ui.revokeconsent.RevokeConsentViewModel.RevokeConsentViewState.*
import java.util.*


class RevokeConsentViewModel: AuthenticationBaseViewModel() {

    enum class RevokeConsentViewState {
        INITIAL_VIEW,
        BIOMETRIC,
        PIN_ENTRY,
        AUTHENTICATION,
        ERROR,
        SIGNED,
        DONE
    }

    var viewState: MutableLiveData<RevokeConsentViewState> = MutableLiveData(INITIAL_VIEW)
    var tgBobfConsent: TgBobfConsent? = null
    var error: MutableLiveData<BaseError> = MutableLiveData()

    private lateinit var getConsentDate: Date

    var userPin: String? = null
        set(value) {
            field = value
            viewState.postValue(AUTHENTICATION)
        }

    fun startPinEntry() {
        viewState.postValue(PIN_ENTRY)
    }

    fun onBiometricFail(message: String) {
        error.postValue(NetworkError.Unknown(message))
        viewState.postValue(PIN_ENTRY)
    }

    fun authenticate() {

        if (signingUser == null || tgBobfConsent == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(ERROR)

            return
        }

        val pinProvider = PinProvider {
            it.consume(userPin)
        }

        val consentType = "action=REVOKE|consentId=${tgBobfConsent?.consent?.consentId}"
        val message = getHash(consentType)
        if (message == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(ERROR)
            return
        }
        getConsentDate = Date()

        getSignature(
            message = message,
            signingUser = signingUser!!,
            date = getConsentDate,
            pinProvider = pinProvider
        ) { result ->
            when (result) {
                is MiraclSuccess -> {
                    viewState.postValue(SIGNED)
                    revokeConsent(result.value)
                }
                is MiraclError -> {
                    error.postValue(NetworkError.Authentication)
                    viewState.postValue(ERROR)
                }
            }
        }
    }

    private fun revokeConsent(signature: Signature) {

        if (signingUser == null || tgBobfConsent == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(ERROR)

            return
        }

        val revokeConsentRequest = RevokeConsentRequest(
            consentId = tgBobfConsent!!.consent.consentId,
            header = Header(
                signature = signature,
                timestamp = getTimeStamp(getConsentDate)
            )
        )

        viewModelScope.launch(IO) {
            RevokeConsentUseCase().execute(revokeConsentRequest) { result ->
                handleResult(
                    result,
                    {
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

    fun startBiometric() {
        viewState.postValue(BIOMETRIC)
    }

}

