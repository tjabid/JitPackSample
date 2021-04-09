package tg.sdk.sca.presentation.ui.sdkdashboard

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import tg.sdk.sca.presentation.core.viewmodel.BaseViewModel
import tg.sdk.sca.presentation.ui.sdkdashboard.SdkDashboardViewModel.SdkDashboardViewState.*
import tg.sdk.sca.tgbobf.TgBobfSdk
import tg.sdk.sca.tgbobf.TgBobfUserTokenManager

class SdkDashboardViewModel: BaseViewModel() {

    enum class SdkDashboardViewState {
        INITIAL_VIEW,
        NOT_ENABLED,
        AUTHENTICATION,
        ENROLLING,
        ENABLED,
        ERROR,
        DONE
    }

    var viewState: MutableLiveData<SdkDashboardViewState> = MutableLiveData(INITIAL_VIEW)
    var error: MutableLiveData<String> = MutableLiveData()
    var userJwtToken: String? = null

    init {
        adjustInitialView()
    }

    fun enableObp() {
        viewState.postValue(AUTHENTICATION)
        viewModelScope.launch(IO) {

            TgBobfSdk.authorizer?.invoke()
            val result = TgBobfUserTokenManager.getUserTokenChannel().asFlow().first()

            handleResult(
                result,
                { userToken ->
                    userToken?.let { userJwtToken = userToken }
                    viewState.postValue(ENROLLING)
                },
                {
                    viewState.postValue(ERROR)
                    error.postValue(it.message ?: "Failed to Authorize!")
                }
            )
        }
    }

    fun adjustInitialView() {
        viewState.postValue(
            if (TgBobfSdk.isUserEnrolled()) {
                ENABLED
            } else {
                NOT_ENABLED
            }
        )
    }
}

