package tg.sdk.sca.presentation.core.viewmodel

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.observe
import androidx.lifecycle.viewModelScope
import tg.sdk.sca.presentation.core.Navigation
import tg.sdk.sca.presentation.core.ViewState
import tg.sdk.sca.presentation.utils.extensions.event
import tg.sdk.sca.presentation.utils.extensions.mutable
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

abstract class BaseStateViewModel<VIEW_STATE : ViewState, NAVIGATION : Navigation> :
    BaseViewModel() {

    protected val viewState = mutable<VIEW_STATE>()
    private val navigationState = event<NAVIGATION>()

    fun observeViewState(owner: LifecycleOwner, observer: (VIEW_STATE) -> Unit) {
        viewState.observe(owner, observer)
        if (viewState.value != null) {
            viewState.postValue(viewState.value)
        }
    }

    fun observeNavigation(owner: LifecycleOwner, observer: (NAVIGATION) -> Unit) =
        navigationState.observe(owner, observer)

    protected fun postViewState(newViewState: VIEW_STATE, delayMillis: Long = 0L): Job {
        return viewModelScope.launch(MAIN) {
            if (delayMillis > 0) delay(delayMillis)
            viewState.value = newViewState
        }
    }

    protected fun navigateTo(navigation: NAVIGATION) {
        navigationState.postValue(navigation)
    }
}