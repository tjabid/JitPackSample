package tg.sdk.sca.presentation.ui.authenticate

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
import tg.sdk.sca.data.consent.RetrieveConsentRequest
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.data.consent.UpdateConsentRequest
import tg.sdk.sca.domain.enrollment.register.GetConsentUseCase
import tg.sdk.sca.domain.enrollment.register.UpdateConsentUseCase
import tg.sdk.sca.presentation.core.viewmodel.AuthenticationBaseViewModel
import tg.sdk.sca.presentation.utils.SdkConstants.BuildFlavour
import java.util.*
import tg.sdk.sca.BuildConfig


class AuthenticationViewModel: AuthenticationBaseViewModel() {

    enum class AuthenticationViewState {
        INITIAL_VIEW,
        BIOMETRIC,
        PIN_ENTRY,
        AUTHENTICATION,
        ERROR,
        SIGNED,
        FETCH_CONSENT,
        LOAD_CONSENT,
        AUTHENTICATION_UPDATE,
        UPDATE_CONSENT,
        DONE
    }

    var viewState: MutableLiveData<AuthenticationViewState> = MutableLiveData(
        AuthenticationViewState.INITIAL_VIEW
    )
    var tgBobfConsent: MutableLiveData<TgBobfConsent> = MutableLiveData()
    var error: MutableLiveData<BaseError> = MutableLiveData()

    private lateinit var getConsentDate: Date
    var documentId: String? = null

    var userPin: String? = null
        set(value) {
            field = value
            viewState.postValue(AuthenticationViewState.AUTHENTICATION)
        }

    fun startPinEntry() {
        viewState.postValue(AuthenticationViewState.PIN_ENTRY)
    }

    fun onBiometricFail(message: String) {
        error.postValue(NetworkError.Unknown(message))
        viewState.postValue(AuthenticationViewState.PIN_ENTRY)
    }

    fun authenticate() {

        if (documentId.isNullOrEmpty()) {
            error.postValue(NetworkError.Unknown("Invalid request!"))
            viewState.postValue(AuthenticationViewState.ERROR)
            return
        }

        if (signingUser == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(AuthenticationViewState.ERROR)

            return
        }

        val pinProvider = PinProvider {
            it.consume(userPin)
        }

        val session = "sessionId=$documentId"
        val message = getHash(session)
        if (message == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(AuthenticationViewState.ERROR)
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
                    viewState.postValue(AuthenticationViewState.FETCH_CONSENT)
                    getConsent(result.value)
                }
                is MiraclError -> {
                    error.postValue(NetworkError.Authentication)
                    viewState.postValue(AuthenticationViewState.ERROR)
                }
            }
        }
    }

    private fun getConsent(signature: Signature) {

        if (documentId.isNullOrEmpty()) {
            error.postValue(NetworkError.Unknown("Invalid request!"))
            viewState.postValue(AuthenticationViewState.ERROR)
            return
        }

        viewState.postValue(AuthenticationViewState.FETCH_CONSENT)

        val retrieveConsentRequest = RetrieveConsentRequest(
            sessionId = documentId!!,
            header = Header(
                signature = signature,
                timestamp = getTimeStamp(getConsentDate)
            )
        )

        viewModelScope.launch(IO) {
            GetConsentUseCase().execute(retrieveConsentRequest) { result ->
                handleResult(
                    result,
                    {
                        tgBobfConsent.postValue(it)
                        viewState.postValue(AuthenticationViewState.LOAD_CONSENT)
                    },
                    {
                        error.postValue(it)
                        viewState.postValue(AuthenticationViewState.ERROR)
                    }
                )
            }
        }
    }

    fun startBiometric() {
        viewState.postValue(AuthenticationViewState.BIOMETRIC)
    }

    fun updateConsentAuthenticate(approved: Boolean, accounts: List<String>) {

        viewState.postValue(AuthenticationViewState.AUTHENTICATION_UPDATE)

        if (signingUser == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(AuthenticationViewState.ERROR)

            return
        }

        val pinProvider = PinProvider {
            it.consume(userPin)
        }

        val sortedAccounts = accounts.sorted()
        val sortedString = sortedAccounts.joinToString(separator = ",") { it }
        val status = if (approved) "APPROVE" else "DECLINE"
        val session = "action=$status|sessionId=$documentId|accountIds=$sortedString"
        val message = getHash(session)
        if (message == null) {
            error.postValue(NetworkError.Authentication)
            viewState.postValue(AuthenticationViewState.ERROR)
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
                    viewState.postValue(AuthenticationViewState.UPDATE_CONSENT)
                    updateConsent(result.value, accounts, status)
                }
                is MiraclError -> {
                    error.postValue(NetworkError.Authentication)
                    viewState.postValue(AuthenticationViewState.ERROR)
                }
            }
        }
    }

    private fun updateConsent(signature: Signature, accounts: List<String>, status: String) {

        val updateConsentRequest = UpdateConsentRequest(
            sessionId = documentId!!,
            header = Header(
                signature = signature,
                timestamp = getTimeStamp(getConsentDate)
            ),
            accountIds = accounts,
            action = status
        )

        viewModelScope.launch(IO) {
            UpdateConsentUseCase().execute(updateConsentRequest) { result ->
                handleResult(
                    result,
                    {
                        viewState.postValue(AuthenticationViewState.DONE)
                    },
                    {
                        error.postValue(it)
                        viewState.postValue(AuthenticationViewState.ERROR)
                    }
                )
            }
        }

    }

    fun isApprovedDisabled() =
        BuildConfig.FLAVOR == BuildFlavour.KFH || BuildConfig.FLAVOR == BuildFlavour.DemoBank

}