package com.miracl.trust.signing

import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.authentication.AuthenticatorContract
import com.miracl.trust.authentication.AuthenticatorScopes
import com.miracl.trust.crypto.Crypto
import com.miracl.trust.crypto.SigningResult
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.SigningUser
import com.miracl.trust.model.isEmpty
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants
import com.miracl.trust.util.toHexString
import java.util.*

internal enum class DocumentSigningErrorResponses(val message: String) {
    EMPTY_MESSAGE_HASH("Empty message hash"),
    EMPTY_SIGNING_IDENTITY("Empty signing identity"),
    INVALID_CRYPTO_VALUES("Invalid crypto values"),
    EMPTY_PUBLIC_KEY("Empty public key"),
    FAIL("Failed to sign a document")
}

@ExperimentalUnsignedTypes
internal class DocumentSigner(
    private val crypto: Crypto,
    private val authenticator: AuthenticatorContract
) : Loggable {
    suspend fun sign(
        message: ByteArray,
        timestamp: Date,
        signingUser: SigningUser,
        pinProvider: PinProvider
    ): MiraclResult<Signature, Error> {
        logOperation(LoggerConstants.FLOW_STARTED)

        validateInputParameters(signingUser, message)?.let { error ->
            return MiraclError(error)
        }

        var pin = crypto.acquirePin(pinProvider)

        val authenticateResponse = authenticator.authenticate(
            signingUser.identity,
            null,
            { it.consume(pin) },
            arrayOf(AuthenticatorScopes.SIGNING_AUTHENTICATION.value),
            signingUser.publicKey
        )
        if (authenticateResponse is MiraclError) {
            return MiraclError(authenticateResponse.value)
        }

        val combinedMpinId = signingUser.identity.mpinId + signingUser.publicKey

        logOperation(LoggerConstants.DocumentSignerOperations.SIGNING)
        val signResponse = crypto.sign(
            message,
            combinedMpinId,
            signingUser.identity.token,
            timestamp,
            signingUser.identity.pinLength
        ) { it.consume(pin) }

        validateCryptoSign(signResponse)?.let { error ->
            return MiraclError(error)
        }

        pin = null

        val signingResult = (signResponse as MiraclSuccess).value
        val signature = Signature(
            signingUser.identity.mpinId.toHexString(),
            signingResult.u.toHexString(),
            signingResult.v.toHexString(),
            signingUser.publicKey.toHexString(),
            signingUser.identity.dtas,
            message.toHexString()
        )

        logOperation(LoggerConstants.FLOW_FINISHED)
        return MiraclSuccess(signature)
    }

    private fun validateInputParameters(
        signingUser: SigningUser,
        message: ByteArray
    ): Error? {
        if (signingUser.identity.isEmpty()) {
            return Error(DocumentSigningErrorResponses.EMPTY_SIGNING_IDENTITY.message)
        }

        if (signingUser.publicKey.isEmpty()) {
            return Error(DocumentSigningErrorResponses.EMPTY_PUBLIC_KEY.message)
        }

        if (message.isEmpty()) {
            return Error(DocumentSigningErrorResponses.EMPTY_MESSAGE_HASH.message)
        }

        return null
    }

    private fun validateCryptoSign(signResponse: MiraclResult<SigningResult, Error>): Error? =
        when (signResponse) {
            is MiraclError -> signResponse.value
            is MiraclSuccess -> {
                if (signResponse.value.u.isEmpty()
                    || signResponse.value.v.isEmpty()
                ) {
                    Error(DocumentSigningErrorResponses.INVALID_CRYPTO_VALUES.message)
                } else {
                    null
                }
            }
        }

    private fun logOperation(operation: String) {
        miraclLogger?.info(LoggerConstants.DOCUMENT_SIGNER_TAG, operation)
    }
}