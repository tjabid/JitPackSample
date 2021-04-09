package tg.sdk.sca.data.consent

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import androidx.annotation.Keep

@Keep
@Parcelize
data class TgBobfConsent(
    val consentType: String?,
    val consent: TgConsent,
    val accounts: List<TgAccount>,
    val tppName: String?
): Parcelable
