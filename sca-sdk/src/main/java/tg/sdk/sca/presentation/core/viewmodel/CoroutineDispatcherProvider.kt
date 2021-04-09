package tg.sdk.sca.presentation.core.viewmodel

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers

object CoroutineDispatcherProvider {
    fun getUseCaseDispatcher(): CoroutineDispatcher = Dispatchers.IO
    fun getMainDispatcher(): CoroutineDispatcher = Dispatchers.Main
    fun getComputationDispatcher(): CoroutineDispatcher = Dispatchers.Default
}