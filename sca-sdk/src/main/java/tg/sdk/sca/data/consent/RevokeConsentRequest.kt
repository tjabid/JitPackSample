package tg.sdk.sca.data.consent

import androidx.annotation.Keep

@Keep
data class RevokeConsentRequest (
    val header: Header,
    val consentId: String
)

