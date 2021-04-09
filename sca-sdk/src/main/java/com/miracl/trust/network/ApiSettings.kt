package com.miracl.trust.network

internal class ApiSettings(baseUrl: String) {
    companion object {
        const val CLIENT_SETTINGS_PATH = "/rps/v2/clientSettings"

        const val VERIFICATION_CONFIRMATION_PATH = "/verification/confirmation"
    }

    val clientSettingsUrl: String = baseUrl.appendPath(CLIENT_SETTINGS_PATH)

    val verificationConfirmationUrl: String = baseUrl.appendPath(VERIFICATION_CONFIRMATION_PATH)

    lateinit var signatureUrl: String

    lateinit var registerUrl: String

    lateinit var pass1Url: String

    lateinit var pass2Url: String

    lateinit var authenticateUrl: String

    lateinit var dvsRegUrl: String

    lateinit var verificationUrl: String
}
