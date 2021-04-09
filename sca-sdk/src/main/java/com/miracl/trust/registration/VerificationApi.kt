package com.miracl.trust.registration

import android.net.Uri
import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.network.*
import com.miracl.trust.util.json.JsonUtil

@Keep
internal data class VerificationRequestBody(
    @Expose val userId: String,
    @Expose val clientId: String,
    @Expose val accessId: String,
    @Expose val deviceName: String
)

@Keep
internal data class ConfirmationRequestBody(
    @Expose val userId: String,
    @Expose val code: String
)

@Keep
data class ActivationToken(
    @Expose @SerializedName("actToken") val value: String,
    @Expose @SerializedName("expireTime") val expiration: Long
)

internal interface VerificationApi {
    suspend fun executeVerificationRequest(
        verificationRequestBody: VerificationRequestBody
    ): MiraclResult<Unit, Error>

    suspend fun executeConfirmationRequest(
        confirmationRequestBody: ConfirmationRequestBody
    ): MiraclResult<ActivationToken, Error>

    fun validateVerificationUrl(
        verificationUrl: String
    ): Error?
}

internal class VerificationApiManager(
    private val jsonUtil: JsonUtil,
    private val httpRequestExecutor: HttpRequestExecutor,
    private val apiSettings: ApiSettings
) : ApiManager(), VerificationApi {
    override suspend fun executeVerificationRequest(verificationRequestBody: VerificationRequestBody): MiraclResult<Unit, Error> {
        try {
            val requestBodyJson = jsonUtil.toJson(verificationRequestBody)
            val apiRequest = ApiRequest(
                method = HttpMethod.POST,
                headers = null,
                body = requestBodyJson,
                params = null,
                url = apiSettings.verificationUrl
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            return MiraclSuccess(Unit)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(
                    ex.message
                        ?: VerificationErrorResponses.INVALID_VERIFICATION_RESPONSE.message
                ),
                exception = ex
            )
        }
    }

    override suspend fun executeConfirmationRequest(confirmationRequestBody: ConfirmationRequestBody): MiraclResult<ActivationToken, Error> {
        try {
            val requestBodyJson = jsonUtil.toJson(confirmationRequestBody)
            val apiRequest = ApiRequest(
                method = HttpMethod.POST,
                headers = null,
                body = requestBodyJson,
                params = null,
                url = apiSettings.verificationConfirmationUrl
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val confirmationResponse =
                jsonUtil.fromJsonString((result as MiraclSuccess).value, ActivationToken::class)

            return MiraclSuccess(confirmationResponse)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(
                    ex.message
                        ?: VerificationErrorResponses.INVALID_CONFIRMATION_RESPONSE.message
                ),
                exception = ex
            )
        }
    }

    override fun validateVerificationUrl(verificationUrl: String): Error? {
        val verificationConfirmationUrl = Uri.parse(apiSettings.verificationConfirmationUrl)
        val verificationUri = Uri.parse(verificationUrl)

        if (verificationUri.host != verificationConfirmationUrl.host) {
            return Error(VerificationErrorResponses.INVALID_CONFIRMATION_HOST.message)
        }

        if (verificationUri.path != verificationConfirmationUrl.path) {
            return Error(VerificationErrorResponses.INVALID_CONFIRMATION_PATH.message)
        }

        return null
    }
}