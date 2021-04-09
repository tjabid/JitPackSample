package tg.sdk.sca.data.consent

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import androidx.annotation.Keep

@Keep
@Parcelize
data class TgAccount(
    val accountId: String,
    val accountNumber: String,
    val currency: String,
    val accountNickname: String,
    val accountName: String,
    val accountType: String,
    val accountSubType: String
): Parcelable