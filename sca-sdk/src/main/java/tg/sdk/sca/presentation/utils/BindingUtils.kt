package tg.sdk.sca.presentation.utils

import android.widget.TextView
import androidx.databinding.BindingAdapter
import tg.sdk.sca.core.utils.TgBobfSdkDateUtils
import tg.sdk.sca.data.consent.TgAccount
import tg.sdk.sca.data.consent.TgBobfConsent

@BindingAdapter("consentAccountTitle")
fun TextView.consentAccountTitle(account: TgAccount?) {
    account?.let { "${it.accountSubType} - ${it.currency}".also { value -> text = value } }
}

@BindingAdapter("consentName")
fun TextView.consentName(consent: TgBobfConsent?) {
//    consent?.consent?.permissions?.let { text = it.toString() }
    consent?.consent?.let { text = "Consent - 1" }
}

@BindingAdapter("consentAccountList")
fun TextView.consentAccountList(accounts: List<TgAccount>?) {
//    text = if (!accounts.isNullOrEmpty()) {
//        val value = accounts.joinToString(separator = "\n\n") {
//            "${it.accountSubType} - ${it.currency}\n${it.accountId}"
//        }
//        value
//    } else {
//        "No accounts"
//    }
}

@BindingAdapter("dateAuthorised")
fun TextView.dateAuthorised(date: String?) {
    date?.let { text = TgBobfSdkDateUtils.parseConsentCreateDate(it) }
}

@BindingAdapter("dateExpires")
fun TextView.dateExpires(date: String?) {
    date?.let { text = TgBobfSdkDateUtils.parseConsentDate(it) }
}
