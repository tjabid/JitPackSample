package com.miracl.trust.authentication

import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.crypto.Crypto
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.Identity
import com.miracl.trust.util.hexStringToByteArray
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants
import com.miracl.trust.util.toHexString

internal enum class AuthenticationResponses(val message: String) {
    FAIL("Authentication fail."),
    FAIL_EMPTY_IDENTITY("Authentication failed because the user object has empty fields."),
    FAIL_INVALID_RESPONSE("Authentication fail because of invalid server response.")
}

internal enum class AuthenticatorScopes(val value: String) {
    SIGNING_REGISTRATION("dvs-reg"),
    SIGNING_AUTHENTICATION("dvs-auth"),
    OIDC("oidc"),
    AUTH_CODE("authcode")
}

internal interface AuthenticatorContract {
    suspend fun authenticate(
        userIdentity: Identity,
        accessId: String?,
        pinProvider: PinProvider,
        scope: Array<String>,
        publicKey: ByteArray? = null
    ): MiraclResult<AuthenticateResponse, Error>
}

@ExperimentalUnsignedTypes
internal class Authenticator(
    private val authenticationApi: AuthenticationApi,
    private val crypto: Crypto
) : AuthenticatorContract, Loggable {
    companion object {
        const val SUCCESS_STATUS = 200
    }

    override suspend fun authenticate(
        userIdentity: Identity,
        accessId: String?,
        pinProvider: PinProvider,
        scope: Array<String>,
        publicKey: ByteArray?
    ): MiraclResult<AuthenticateResponse, Error> {
        logOperation(LoggerConstants.FLOW_STARTED)

        try {
            if (userIdentity.isEmpty()) {
                return MiraclError(Error(AuthenticationResponses.FAIL_EMPTY_IDENTITY.message))
            }

            var combinedMpinId = userIdentity.mpinId
            if (scope.contains(AuthenticatorScopes.SIGNING_AUTHENTICATION.value) && publicKey != null) {
                combinedMpinId += publicKey
            }

            // Client 1
            logOperation(LoggerConstants.AuthenticatorOperations.CLIENT_PASS_1_PROOF)
            val pass1ProofResult = crypto.getClientPass1Proof(
                combinedMpinId,
                userIdentity.token,
                userIdentity.pinLength,
                pinProvider
            )
            if (pass1ProofResult is MiraclError) {
                return MiraclError(pass1ProofResult.value)
            }

            val pass1Proof = (pass1ProofResult as MiraclSuccess).value

            // Server 1
            val mpinId = userIdentity.mpinId.toHexString()
            val u = pass1Proof.U.toHexString()
            var publicKeyHex: String? = null

            if (scope.contains(AuthenticatorScopes.SIGNING_AUTHENTICATION.value)) {
                publicKeyHex = publicKey?.toHexString()
            }

            val pass1RequestBody = Pass1RequestBody(
                mpinId = mpinId,
                dtas = userIdentity.dtas,
                U = u,
                scope = scope,
                publicKey = publicKeyHex
            )

            logOperation(LoggerConstants.AuthenticatorOperations.CLIENT_PASS_1_REQUEST)
            val pass1ResponseResult = authenticationApi.executePass1Request(pass1RequestBody)
            if (pass1ResponseResult is MiraclError) {
                return MiraclError(pass1ResponseResult.value)
            }

            val pass1Response = (pass1ResponseResult as MiraclSuccess).value

            // Client 2
            val y = pass1Response.Y.hexStringToByteArray()

            logOperation(LoggerConstants.AuthenticatorOperations.CLIENT_PASS_2_PROOF)
            val pass2ProofResult =
                crypto.getClientPass2Proof(
                    pass1Proof.X,
                    y,
                    pass1Proof.SEC
                )
            if (pass2ProofResult is MiraclError) {
                return MiraclError(pass2ProofResult.value)
            }

            val pass2Proof = (pass2ProofResult as MiraclSuccess).value

            // Server 2
            val v = pass2Proof.V.toHexString()
            val pass2RequestBody = Pass2RequestBody(
                mpinId = mpinId,
                accessId = accessId,
                V = v
            )

            logOperation(LoggerConstants.AuthenticatorOperations.CLIENT_PASS_2_REQUEST)
            val pass2ResponseResult = authenticationApi.executePass2Request(pass2RequestBody)
            if (pass2ResponseResult is MiraclError) {
                return MiraclError(pass2ResponseResult.value)
            }

            val pass2Response = (pass2ResponseResult as MiraclSuccess).value

            // Authenticate
            val authenticateRequest =
                AuthenticateRequestBody(
                    authOtt = pass2Response.authOtt
                )

            logOperation(LoggerConstants.AuthenticatorOperations.AUTHENTICATE_REQUEST)
            val authenticationResponseResult =
                authenticationApi.executeAuthenticateRequest(authenticateRequest)
            if (authenticationResponseResult is MiraclError) {
                return MiraclError(authenticationResponseResult.value)
            }

            val authenticateResponse = (authenticationResponseResult as MiraclSuccess).value
            if (authenticateResponse.status == SUCCESS_STATUS) {
                logOperation(LoggerConstants.FLOW_FINISHED)

                return MiraclSuccess(authenticateResponse)
            }

            return MiraclError(Error(AuthenticationResponses.FAIL.message))
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(ex.message ?: AuthenticationResponses.FAIL.message),
                exception = ex
            )
        }
    }

    private fun logOperation(operation: String) {
        miraclLogger?.info(LoggerConstants.AUTHENTICATOR_TAG, operation)
    }
}

internal fun Identity.isEmpty(): Boolean = dtas.isBlank() || mpinId.isEmpty() || token.isEmpty()