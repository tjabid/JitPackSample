package tg.sdk.sca.data.consent

import androidx.annotation.Keep

@Keep
data class RetrieveConsentRequest (
    val header: Header,
    val sessionId: String
)

