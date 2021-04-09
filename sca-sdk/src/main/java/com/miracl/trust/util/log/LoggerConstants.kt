package com.miracl.trust.util.log

object LoggerConstants {
    internal const val VERIFICATOR_TAG = "Verificator"
    internal const val REGISTRATOR_TAG = "Registrator"
    internal const val AUTHENTICATOR_TAG = "Authenticator"
    internal const val SIGNING_REGISTRATOR_TAG = "SigningRegistrator"
    internal const val DOCUMENT_SIGNER_TAG = "DocumentSigner"

    internal const val FLOW_STARTED = "Flow started."
    internal const val FLOW_FINISHED = "Flow finished."
    internal const val FLOW_ERROR = "Flow finished with error = %s"

    internal const val CRYPTO_TAG = "Crypto"
    internal const val CRYPTO_OPERATION_STARTED = "Crypto operation %s has started."
    internal const val CRYPTO_OPERATION_FINISHED = "Crypto operation %s has finished."

    internal const val NETWORK_TAG = "Network"
    internal const val NETWORK_REQUEST = "%s %s"
    internal const val NETWORK_RESPONSE = "%s %s Status code: %d"

    object VerificatorOperations {
        internal const val VERIFY_REQUEST = "Executing verify request."
        internal const val ACTIVATION_TOKEN_REQUEST = "Executing activation token request."
    }

    object RegistratorOperations {
        internal const val REGISTER_REQUEST = "Executing register request."
        internal const val SIGNATURE_REQUEST = "Executing signature request."
        internal const val CLIENT_SECRET_REQUEST = "Executing client secret request request."
        internal const val CLIENT_TOKEN = "Getting client token."
        internal const val SAVING_USER = "Saving authentication user to the database."
    }

    object AuthenticatorOperations {
        internal const val CLIENT_PASS_1_PROOF = "Getting client pass proof 1."
        internal const val CLIENT_PASS_1_REQUEST = "Execute pass 1 request."
        internal const val CLIENT_PASS_2_PROOF = "Getting client pass proof 2."
        internal const val CLIENT_PASS_2_REQUEST = "Executing pass 2 request."
        internal const val AUTHENTICATE_REQUEST = "Executing authenticate request."
    }

    object SigningRegistrationOperations {
        internal const val SIGNING_KEY_PAIR = "Getting signing key pair."
        internal const val DVS_CLIENT_SECRET_1_REQUEST = "Executing DVS client secret 1 request."
        internal const val DVS_CLIENT_SECRET_2_REQUEST = "Executing DVS client secret 2 request."
        internal const val SIGNING_CLIENT_TOKEN = "Getting signing client token."
    }

    object DocumentSignerOperations {
        internal const val SIGNING = "Signing."
    }
}