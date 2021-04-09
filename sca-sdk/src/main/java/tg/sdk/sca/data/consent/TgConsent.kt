package tg.sdk.sca.data.consent

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.android.parcel.Parcelize
import androidx.annotation.Keep

@Keep
@Parcelize
data class TgConsent(
    val interactionId: String?,
    @SerializedName("Permissions")
    val permissions: List<String>,
    @SerializedName("TransactionFromDateTime")
    val transactionFromDateTime: String,
    @SerializedName("TransactionToDateTime")
    val transactionToDateTime: String,
    @SerializedName("ConsentId")
    val consentId: String,
    @SerializedName("CreationDateTime")
    val creationDateTime: String,
    @SerializedName("Status")
    val status: String,
    @SerializedName("StatusUpdateDateTime")
    val statusUpdateDateTime: String,
    @SerializedName("Initiation")
    val initiation: Initiation?
): Parcelable