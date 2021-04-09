package tg.sdk.sca.data.consent

import com.miracl.trust.signing.Signature
import androidx.annotation.Keep

@Keep
data class Header(
    val signature: Signature,
    val timestamp: Int,
    val type: String = "verification"
)