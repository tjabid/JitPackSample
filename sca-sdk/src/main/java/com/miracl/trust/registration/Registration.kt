package com.miracl.trust.registration

import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.crypto.Crypto
import com.miracl.trust.crypto.SupportedEllipticCurves
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.AuthenticationUser
import com.miracl.trust.model.Identity
import com.miracl.trust.storage.UserStorage
import com.miracl.trust.util.getCurrentTimeUnixTimestamp
import com.miracl.trust.util.hexStringToByteArray
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants

internal enum class RegistrationResponses(val message: String) {
    FAIL("Registration fail"),
    FAIL_EMPTY_USER_ID("Empty user ID."),
    FAIL_EMPTY_DEVICE_NAME("Empty device name."),
    FAIL_INVALID_ACTIVATION_TOKEN("Invalid activationToken."),
    FAIL_EXPIRED_ACTIVATION_TOKEN("Expired activationToken."),
    FAIL_SAVE_USER("Registration fail because could not save user."),
    FAIL_USER_ALREADY_REGISTERED("Registration fail because user with the same userId is already registered."),
    UNSUPPORTED_ELLIPTIC_CURVE("Unsupported elliptic curve")
}

internal interface RegistratorContract {
    suspend fun register(
        userId: String,
        activationToken: ActivationToken,
        pinProvider: PinProvider,
        deviceName: String,
        pushNotificationsToken: String?
    ): MiraclResult<AuthenticationUser, Error>
}

@ExperimentalUnsignedTypes
internal class Registrator(
    private val registrationApi: RegistrationApi,
    private val crypto: Crypto,
    private val userStorage: UserStorage
) : RegistratorContract, Loggable {
    override suspend fun register(
        userId: String,
        activationToken: ActivationToken,
        pinProvider: PinProvider,
        deviceName: String,
        pushNotificationsToken: String?
    ): MiraclResult<AuthenticationUser, Error> {
        logOperation(LoggerConstants.FLOW_STARTED)

        validateInput(userId, activationToken, deviceName)?.let { error ->
            return MiraclError(error)
        }

        val registerRequestBody = RegisterRequestBody(
            userId = userId.trim(),
            deviceName = deviceName.trim(),
            activationToken = activationToken.value.trim(),
            pushToken = pushNotificationsToken
        )

        try {
            logOperation(LoggerConstants.RegistratorOperations.REGISTER_REQUEST)
            val registerResponseResult = registrationApi.executeRegisterRequest(registerRequestBody)
            if (registerResponseResult is MiraclError) {
                return MiraclError(registerResponseResult.value)
            }

            val registerResponse = (registerResponseResult as MiraclSuccess).value
            logOperation(LoggerConstants.RegistratorOperations.SIGNATURE_REQUEST)
            val signatureResponseResult = registrationApi.executeSignatureRequest(registerResponse)
            if (signatureResponseResult is MiraclError) {
                return MiraclError(signatureResponseResult.value)
            }

            val signatureResponse = (signatureResponseResult as MiraclSuccess).value

            if (!SupportedEllipticCurves.values().map { it.name }
                    .contains(signatureResponse.curve)) {
                return MiraclError(Error(RegistrationResponses.UNSUPPORTED_ELLIPTIC_CURVE.message))
            }

            logOperation(LoggerConstants.RegistratorOperations.CLIENT_SECRET_REQUEST)
            val clientSecretResponseResult =
                registrationApi.executeClientSecretRequest(signatureResponse.clientSecret2Url)
            if (clientSecretResponseResult is MiraclError) {
                return MiraclError(clientSecretResponseResult.value)
            }

            val clientSecretShare2Response = (clientSecretResponseResult as MiraclSuccess).value
            val mpinId = registerResponse.mpinId.hexStringToByteArray()

            logOperation(LoggerConstants.RegistratorOperations.CLIENT_TOKEN)

            val tokenResult = crypto.getClientToken(
                mpinId = mpinId,
                clientSecretShare1 = signatureResponse.clientSecretShare.hexStringToByteArray(),
                clientSecretShare2 = clientSecretShare2Response.clientSecretShare2.hexStringToByteArray(),
                requiredPinLength = registerResponse.pinLength,
                pinProvider = pinProvider
            )

            signatureResponse.clientSecretShare = ""
            clientSecretShare2Response.clientSecretShare2 = ""

            if (tokenResult is MiraclError) {
                return MiraclError(tokenResult.value)
            }

            val token = (tokenResult as MiraclSuccess).value

            val user = AuthenticationUser(
                Identity(
                    userId = registerRequestBody.userId,
                    pinLength = registerResponse.pinLength,
                    mpinId = mpinId,
                    dtas = signatureResponse.dtas,
                    token = token,
                    isBlocked = false
                )
            )

            logOperation(LoggerConstants.RegistratorOperations.SAVING_USER)

            return saveUser(user)
        } catch (ex: NumberFormatException) {
            return MiraclError(
                value = Error(ex.message ?: RegistrationResponses.FAIL.message),
                exception = ex
            )
        } catch (ex: java.lang.Exception) {
            return MiraclError(
                value = Error(ex.message ?: RegistrationResponses.FAIL.message),
                exception = ex
            )
        }
    }

    private fun validateInput(
        userId: String,
        activationToken: ActivationToken,
        deviceName: String
    ): Error? {
        if (userId.isBlank()) {
            return Error(RegistrationResponses.FAIL_EMPTY_USER_ID.message)
        }

        if (deviceName.isBlank()) {
            return Error(RegistrationResponses.FAIL_EMPTY_DEVICE_NAME.message)
        }

        if (activationToken.value.isBlank()) {
            return Error(RegistrationResponses.FAIL_INVALID_ACTIVATION_TOKEN.message)
        }

        if (isExpiredActivationToken(activationToken.expiration)) {
            return Error(RegistrationResponses.FAIL_EXPIRED_ACTIVATION_TOKEN.message)
        }

        return null
    }

    private fun isExpiredActivationToken(expirationTime: Long): Boolean {
        val now = getCurrentTimeUnixTimestamp()

        return expirationTime < now
    }

    private fun saveUser(authenticationUser: AuthenticationUser): MiraclResult<AuthenticationUser, Error> {
        return try {
            userStorage.add(authenticationUser)

            logOperation(LoggerConstants.FLOW_FINISHED)
            MiraclSuccess(authenticationUser)
        } catch (ex: Exception) {
            MiraclError(
                value = Error(ex.message ?: RegistrationResponses.FAIL_SAVE_USER.message),
                exception = ex
            )
        }
    }

    private fun logOperation(operation: String) {
        miraclLogger?.info(LoggerConstants.REGISTRATOR_TAG, operation)
    }
}
