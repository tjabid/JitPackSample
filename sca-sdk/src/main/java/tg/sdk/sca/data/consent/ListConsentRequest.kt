package tg.sdk.sca.data.consent

import tg.sdk.sca.presentation.utils.SdkConstants.CONSENT_TYPE_AIS
import androidx.annotation.Keep

@Keep
data class ListConsentRequest (
    val header: Header,
    val consentType: String = CONSENT_TYPE_AIS
)

