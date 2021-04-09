package tg.sdk.sca.presentation.core.broadcast

import androidx.annotation.StringDef
import tg.sdk.sca.presentation.core.broadcast.BroadcastAction.Companion.ACCOUNT_DISCONNECTED
import tg.sdk.sca.presentation.core.broadcast.BroadcastAction.Companion.APPROVE_CONNECTION_REQUEST
import tg.sdk.sca.presentation.core.broadcast.BroadcastAction.Companion.APP_IN_FOREGROUND

@StringDef(ACCOUNT_DISCONNECTED, APP_IN_FOREGROUND, APPROVE_CONNECTION_REQUEST)
annotation class BroadcastAction {
    companion object {
        const val ACCOUNT_DISCONNECTED = "account_disconnected"
        const val APP_IN_FOREGROUND = "app_in_foreground"
        const val APPROVE_CONNECTION_REQUEST = "approve_connection_request"
    }
}