package com.miracl.trust.registration

import android.net.Uri
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants

internal enum class VerificationErrorResponses(val message: String) {
    EMPTY_USER_ID("Empty user ID."),
    EMPTY_ACCESS_ID("Empty access ID."),
    EMPTY_ACTIVATION_CODE("Empty activation code."),
    FAIL_EMPTY_DEVICE_NAME("Empty device name."),
    INVALID_VERIFICATION_RESPONSE("Invalid verification response."),
    INVALID_CONFIRMATION_RESPONSE("Invalid confirmation response."),
    FAIL_ACTIVATION_TOKEN("Failed to get activation token."),
    INVALID_CONFIRMATION_HOST("Invalid confirmation host."),
    INVALID_CONFIRMATION_PATH("Invalid confirmation path."),
    FAIL("Failed to verify user.")
}

internal class Verificator(
    private val verificationApi: VerificationApi
) : Loggable {
    suspend fun verify(
        userId: String,
        clientId: String,
        accessId: String,
        deviceName: String
    ): MiraclResult<Unit, Error> {
        logOperation(LoggerConstants.FLOW_STARTED)

        validateVerifyInput(accessId, deviceName)?.let { error ->
            return MiraclError(error)
        }

        val verificationRequestBody = VerificationRequestBody(
            userId,
            clientId,
            accessId,
            deviceName
        )

        logOperation(LoggerConstants.VerificatorOperations.VERIFY_REQUEST)
        val verificationResult =
            verificationApi.executeVerificationRequest(verificationRequestBody)

        logOperation(LoggerConstants.FLOW_FINISHED)
        return verificationResult
    }

    suspend fun getActivationToken(
        verificaitonUrl: String
    ): MiraclResult<ActivationTokenResponse, Error> {
        logOperation(LoggerConstants.FLOW_STARTED)

        verificationApi.validateVerificationUrl(verificaitonUrl)?.let { error ->
            return MiraclError(error)
        }

        val verificationUri = Uri.parse(verificaitonUrl)
        val userId = verificationUri.getQueryParameter("user_id") ?: ""
        val code = verificationUri.getQueryParameter("code") ?: ""

        validateActivationTokenInput(userId, code)?.let { error ->
            return MiraclError(error)
        }

        val confirmationRequestBody = ConfirmationRequestBody(
            userId,
            code
        )

        logOperation(LoggerConstants.VerificatorOperations.ACTIVATION_TOKEN_REQUEST)
        val confirmationResult =
            verificationApi.executeConfirmationRequest(confirmationRequestBody)

        if (confirmationResult is MiraclError) {
            return MiraclError(confirmationResult.value)
        }
        val activationToken = (confirmationResult as MiraclSuccess).value

        logOperation(LoggerConstants.FLOW_FINISHED)
        return MiraclSuccess(ActivationTokenResponse(userId, activationToken))
    }

    private fun validateVerifyInput(accessId: String, deviceName: String): Error? {
        if (accessId.isBlank()) {
            return Error(VerificationErrorResponses.EMPTY_ACCESS_ID.message)
        }

        if (deviceName.isBlank()) {
            return Error(VerificationErrorResponses.FAIL_EMPTY_DEVICE_NAME.message)
        }

        return null
    }

    private fun validateActivationTokenInput(userId: String, code: String): Error? {
        if (userId.isBlank()) {
            return Error(VerificationErrorResponses.EMPTY_USER_ID.message)
        }

        if (code.isBlank()) {
            return Error(VerificationErrorResponses.EMPTY_ACTIVATION_CODE.message)
        }

        return null
    }

    private fun logOperation(operation: String) {
        miraclLogger?.info(LoggerConstants.VERIFICATOR_TAG, operation)
    }
}