package tg.sdk.sca.presentation.ui.manageconsent

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
import tg.sdk.sca.data.consent.ListConsentRequest
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.domain.enrollment.register.GetListConsentUseCase
import tg.sdk.sca.presentation.core.viewmodel.AuthenticationBaseViewModel
import tg.sdk.sca.presentation.ui.manageconsent.ManageConsentViewModel.ManageConsentViewState.*
import tg.sdk.sca.presentation.utils.SdkConstants.CONSENT_TYPE_AIS
import java.util.*


class ManageConsentViewModel: AuthenticationBaseViewModel() {

    enum class ManageConsentViewState {
        INITIAL_VIEW,
        BIOMETRIC,
        PIN_ENTRY,
        AUTHENTICATION,
        ERROR,
        SIGNED,
        FETCH_CONSENT,
        LOAD_CONSENT,
        DONE
    }

    var viewState: MutableLiveData<ManageConsentViewState> = MutableLiveData(INITIAL_VIEW)
    var tgBobfConsent: MutableLiveData<List<TgBobfConsent>> = MutableLiveData()
    var error: MutableLiveData<BaseError> = MutableLiveData(null)

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

        if (signingUser == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(ERROR)

            return
        }

        val pinProvider = PinProvider {
            it.consume(userPin)
        }

        val consentType = "consentType=$CONSENT_TYPE_AIS"
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
                    getConsent(result.value)
                }
                is MiraclError -> {
                    error.postValue(NetworkError.Authentication)
                    viewState.postValue(ERROR)
                }
            }
        }
    }

    private fun getConsent(signature: Signature) {

        viewState.postValue(FETCH_CONSENT)

        val listConsentRequest = ListConsentRequest(
                consentType = CONSENT_TYPE_AIS,
                header = Header(
                        signature = signature,
                        timestamp = getTimeStamp(getConsentDate)
                )
        )

        viewModelScope.launch(IO) {
            GetListConsentUseCase().execute(listConsentRequest) { result ->
                handleResult(
                        result,
                        {
                            tgBobfConsent.postValue(it)
                            viewState.postValue(LOAD_CONSENT)
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

