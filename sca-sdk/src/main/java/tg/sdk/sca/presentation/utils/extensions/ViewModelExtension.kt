package tg.sdk.sca.presentation.utils.extensions

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import tg.sdk.sca.presentation.core.viewmodel.SingleLiveEvent

@Suppress("unused")
fun <T> ViewModel.readonly(initial: T? = null): LiveData<T> {
    return MutableLiveData<T>().apply {
        initial?.let { value = it }
    }
}

@Suppress("unused")
fun <T> ViewModel.mutable(initial: T? = null): MutableLiveData<T> {
    return MutableLiveData<T>().apply {
        initial?.let { value = it }
    }
}

@Suppress("unused")
fun <T : Any?> ViewModel.event() = SingleLiveEvent<T>()
