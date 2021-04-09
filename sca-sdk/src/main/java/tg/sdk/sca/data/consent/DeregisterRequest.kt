package tg.sdk.sca.data.consent

import androidx.annotation.Keep

@Keep
data class DeregisterRequest (
    val header: Header,
    val mpinId: String
)

