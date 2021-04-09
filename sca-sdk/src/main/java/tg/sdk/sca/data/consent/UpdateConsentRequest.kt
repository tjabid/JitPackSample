package tg.sdk.sca.data.consent

import androidx.annotation.Keep

@Keep
data class UpdateConsentRequest (
    val header: Header,
    val sessionId: String,
    val accountIds: List<String>,
    val action: String
)