package tg.sdk.sca.presentation.utils

internal object SdkConstants {

    const val SCA_SDK_SHARED_PREFS_FILENAME = "SCA_SDK_SHARED_PREFS"

    const val KEY_PREF_BIOMETRIC_SKIP = "KEY_PREF_BIOMETRIC_SKIP"
    const val KEY_PREF_USER_ID = "KEY_PREF_USER_ID"

    const val KEY_PUSH_TOKEN = "KEY_PUSH_TOKEN"

    const val CONSENT_TYPE_AIS = "AIS"
    const val CONSENT_TYPE_PIS = "PIS"
    const val CONSENT_TYPE_PIS_DOMESTIC = "domestic-payment-consents"

    const val PIN_LENGTH = 4

    var FORCE_USE_REDB = false

    object BuildFlavour {
        const val DemoBank = "demobank"
        const val KFH = "kfh"
    }
}