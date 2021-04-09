package tg.sdk.sca.data.consent

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize
import androidx.annotation.Keep

@Keep
@Parcelize
data class Initiation(
    val InstructedAmount: InstructedAmount,
    val CreditorAccount: CreditorAccount,
): Parcelable

@Keep
@Parcelize
data class InstructedAmount(
    val Amount: String,
    val Currency: String
): Parcelable

@Keep
@Parcelize
data class CreditorAccount(
    val Identification: String,
    val Name: String
): Parcelable