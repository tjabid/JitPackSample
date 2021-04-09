package com.miracl.trust.signing

import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.authentication.AuthenticatorContract
import com.miracl.trust.authentication.AuthenticatorScopes
import com.miracl.trust.authentication.isEmpty
import com.miracl.trust.crypto.Crypto
import com.miracl.trust.crypto.SigningKeyPair
import com.miracl.trust.crypto.SupportedEllipticCurves
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.AuthenticationUser
import com.miracl.trust.model.Identity
import com.miracl.trust.model.SigningUser
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.util.hexStringToByteArray
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants
import com.miracl.trust.util.toHexString

internal enum class SigningRegistrationErrorResponses(val message: String) {
    EMPTY_USER_ID("Empty user ID."),
    EMPTY_IDENTITY("Empty identity."),
    EMPTY_ACCESS_ID("Empty access ID."),
    FAIL_EMPTY_DEVICE_NAME("Empty device name."),
    EMPTY_AUTHENTICATION_RESPONSE("Empty token from authentication response."),
    FAIL_SIGNING_USER_ALREADY_REGISTERED("Registration fail because user with the same userId is already registered."),
    INVALID_CRYPTO_VALUE("Invalid crypto value."),
    UNSUPPORTED_ELLIPTIC_CURVE("Unsupported elliptic curve."),
    INVALID_DVS_CLIENT_1_SECRET_RESPONSE("Invalid DVS client1Secret 1 response."),
    INVALID_DVS_CLIENT_2_SECRET_RESPONSE("Invalid DVS client1Secret 2 response."),
    INVALID_DVS_CLIENT_TOKEN("Invalid DVS client token."),
    FAIL_SAVE_USER("Could not save the signing user."),
    FAIL("Failed to register for signing.")
}

@ExperimentalUnsignedTypes
internal class SigningRegistrator(
    private val signingRegistrationApi: SigningRegistrationApi,
    private val crypto: Crypto,
    private val userStorage: UserStorage,
    private val authenticator: AuthenticatorContract
) : Loggable {
    suspend fun register(
        authenticationUser: AuthenticationUser,
        accessId: String,
        pinProvider: PinProvider,
        signPinProvider: PinProvider,
        deviceName: String
    ): MiraclResult<SigningUser, Error> {
        logOperation(LoggerConstants.FLOW_STARTED)

        validateInputParameters(authenticationUser, accessId, deviceName)?.let { error ->
            return MiraclError(error)
        }

        val authenticateResponse = authenticator.authenticate(
            authenticationUser.identity,
            accessId,
            pinProvider,
            arrayOf(AuthenticatorScopes.SIGNING_REGISTRATION.value)
        )
        if (authenticateResponse is MiraclError) {
            return MiraclError(authenticateResponse.value)
        }

        val dvsRegistrationToken = (authenticateResponse as MiraclSuccess).value.dvsRegister?.token
            ?: return MiraclError(
                Error(SigningRegistrationErrorResponses.EMPTY_AUTHENTICATION_RESPONSE.message)
            )

        logOperation(LoggerConstants.SigningRegistrationOperations.SIGNING_KEY_PAIR)
        val signingKeyPairResult = crypto.generateSigningKeyPair()
        validateSigningKeyPairResponse(signingKeyPairResult)?.let { error ->
            return MiraclError(error)
        }
        val signingKeyPair = (signingKeyPairResult as MiraclSuccess).value

        logOperation(LoggerConstants.SigningRegistrationOperations.DVS_CLIENT_SECRET_1_REQUEST)
        val clientSecret1Response = signingRegistrationApi.executeDVSClientSecret1Request(
            signingKeyPair.publicKey.toHexString(),
            dvsRegistrationToken,
            deviceName
        )
        validateClientSecret1Response(clientSecret1Response)?.let { error ->
            return MiraclError(error)
        }

        logOperation(LoggerConstants.SigningRegistrationOperations.DVS_CLIENT_SECRET_2_REQUEST)
        val clientSecret2Response = signingRegistrationApi.executeDVSClientSecret2Request(
            (clientSecret1Response as MiraclSuccess).value.cs2Url
        )
        validateClientSecret2Response(clientSecret2Response)?.let { error ->
            return MiraclError(error)
        }

        val combinedMpinId =
            clientSecret1Response.value.mpinId.trim()
                .hexStringToByteArray() + signingKeyPair.publicKey

        logOperation(LoggerConstants.SigningRegistrationOperations.SIGNING_CLIENT_TOKEN)
        val dvsClientTokenResponse = crypto.getSigningClientToken(
            clientSecret1Response.value.dvsClientSecretShare.trim().hexStringToByteArray(),
            (clientSecret2Response as MiraclSuccess).value.dvsClientSecret.trim()
                .hexStringToByteArray(),
            signingKeyPair.privateKey,
            combinedMpinId,
            authenticationUser.identity.pinLength,
            signPinProvider
        )
        validateDVSClientToken(dvsClientTokenResponse)?.let { error ->
            return MiraclError(error)
        }

        val dvsClientToken = (dvsClientTokenResponse as MiraclSuccess).value

        val signingUser = SigningUser(
            Identity(
                userId = authenticationUser.identity.userId,
                pinLength = 4,
                isBlocked = false,
                mpinId = clientSecret1Response.value.mpinId.trim().hexStringToByteArray(),
                token = dvsClientToken,
                dtas = clientSecret1Response.value.dtas.trim()
            ),
            signingKeyPair.publicKey
        )

        logOperation(LoggerConstants.FLOW_FINISHED)
        return saveSigningUser(signingUser)
    }

    private fun validateInputParameters(
        authenticationUser: AuthenticationUser,
        accessId: String,
        deviceName: String
    ): Error? {
        val identity = authenticationUser.identity
        if (identity.userId.isBlank()) {
            return Error(
                SigningRegistrationErrorResponses.EMPTY_USER_ID.message
            )
        }

        if (identity.isEmpty()) {
            return Error(
                SigningRegistrationErrorResponses.EMPTY_IDENTITY.message
            )
        }

        if (userStorage.signingUserExists(identity.userId)) {
            return Error(
                SigningRegistrationErrorResponses.FAIL_SIGNING_USER_ALREADY_REGISTERED.message
            )
        }

        if (accessId.isBlank()) {
            return Error(
                SigningRegistrationErrorResponses.EMPTY_ACCESS_ID.message
            )
        }

        if (deviceName.isBlank()) {
            return Error(
                SigningRegistrationErrorResponses.FAIL_EMPTY_DEVICE_NAME.message
            )
        }

        return null
    }

    private fun validateSigningKeyPairResponse(signingKeyPairResult: MiraclResult<SigningKeyPair, Error>): Error? =
        when (signingKeyPairResult) {
            is MiraclError -> signingKeyPairResult.value
            is MiraclSuccess -> {
                if (signingKeyPairResult.value.publicKey.isEmpty()
                    || signingKeyPairResult.value.privateKey.isEmpty()
                ) {
                    Error(SigningRegistrationErrorResponses.INVALID_CRYPTO_VALUE.message)
                } else {
                    null
                }
            }
        }

    private fun validateClientSecret1Response(
        clientSecret1Response: MiraclResult<DVSClientSecret1Response, Error>
    ): Error? =
        when (clientSecret1Response) {
            is MiraclError -> clientSecret1Response.value
            is MiraclSuccess -> {
                if (!SupportedEllipticCurves.values().map { it.name }
                        .contains(clientSecret1Response.value.curve)) {
                    Error(
                        SigningRegistrationErrorResponses.UNSUPPORTED_ELLIPTIC_CURVE.message
                    )
                } else if (clientSecret1Response.value.mpinId.isBlank()
                    || clientSecret1Response.value.dtas.isBlank()
                    || clientSecret1Response.value.dvsClientSecretShare.isBlank()
                    || clientSecret1Response.value.cs2Url.isBlank()
                ) {
                    Error(
                        SigningRegistrationErrorResponses.INVALID_DVS_CLIENT_1_SECRET_RESPONSE.message
                    )
                } else {
                    null
                }
            }
        }

    private fun validateClientSecret2Response(
        clientSecret2Response: MiraclResult<DVSClientSecret2Response, Error>
    ): Error? =
        when (clientSecret2Response) {
            is MiraclError -> clientSecret2Response.value
            is MiraclSuccess -> {
                if (clientSecret2Response.value.dvsClientSecret.isBlank()) {
                    Error(
                        SigningRegistrationErrorResponses.INVALID_DVS_CLIENT_2_SECRET_RESPONSE.message
                    )
                } else {
                    null
                }
            }
        }

    private fun validateDVSClientToken(dvsClientTokenResponse: MiraclResult<ByteArray, Error>): Error? =
        when (dvsClientTokenResponse) {
            is MiraclError -> dvsClientTokenResponse.value
            is MiraclSuccess -> {
                if (dvsClientTokenResponse.value.isEmpty()) {
                    Error(
                        SigningRegistrationErrorResponses.INVALID_DVS_CLIENT_TOKEN.message
                    )
                } else {
                    null
                }
            }
        }

    private fun saveSigningUser(signingUser: SigningUser): MiraclResult<SigningUser, Error> {
        return try {
            userStorage.add(signingUser)
            MiraclSuccess(signingUser)
        } catch (ex: Exception) {
            MiraclError(
                value = Error(
                    ex.message ?: SigningRegistrationErrorResponses.FAIL_SAVE_USER.message
                ),
                exception = ex
            )
        }
    }

    private fun logOperation(operation: String) {
        miraclLogger?.info(LoggerConstants.SIGNING_REGISTRATOR_TAG, operation)
    }
}