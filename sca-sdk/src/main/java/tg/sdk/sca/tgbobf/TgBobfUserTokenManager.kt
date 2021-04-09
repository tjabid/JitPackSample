package tg.sdk.sca.tgbobf

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ConflatedBroadcastChannel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.common.network.Result

internal object TgBobfUserTokenManager {

    private val scope = CoroutineScope(IO + Job())

    private var userTokenChannel: ConflatedBroadcastChannel<Result<String>>? = null

    private var job: Job? = null

    fun onUserTokenSuccessful(userToken: String) {
        job.cancelIfActive()
        job = scope.launch {
            delay(500)
            userTokenChannel?.send(Result.Success(userToken))
        }
    }

    fun onUserTokenError(message: String) {
        job.cancelIfActive()
        job = scope.launch {
            delay(500)
            userTokenChannel?.send(Result.Error(NetworkError.Unknown(message)))
        }
    }

    fun getUserTokenChannel(): ConflatedBroadcastChannel<Result<String>> {
        userTokenChannel = ConflatedBroadcastChannel()
        return userTokenChannel!!
    }

    private fun Job?.cancelIfActive() {
        if (this != null && isActive) cancel()
    }
}