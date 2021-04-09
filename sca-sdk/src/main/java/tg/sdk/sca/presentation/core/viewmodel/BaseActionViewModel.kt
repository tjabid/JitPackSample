package tg.sdk.sca.presentation.core.viewmodel

import androidx.lifecycle.LiveData

open class BaseActionViewModel
/*@Inject*/ constructor() : BaseViewModel() {

    private val _onPositiveClickEvent = SingleLiveEvent<Unit>()
    private val _onNegativeClickEvent = SingleLiveEvent<Unit>()
    private val _onCornerIconClickEvent = SingleLiveEvent<Unit>()

    val onPositiveClickEvent: LiveData<Unit> = _onPositiveClickEvent
    val onNegativeClickEvent: LiveData<Unit> = _onNegativeClickEvent
    val onCornerIconClickEvent: LiveData<Unit> = _onCornerIconClickEvent

    fun onPositiveClick() =
        _onPositiveClickEvent.call()

    fun onNegativeClick() =
        _onNegativeClickEvent.call()

    fun onCornerIconClick() =
        _onCornerIconClickEvent.call()
}