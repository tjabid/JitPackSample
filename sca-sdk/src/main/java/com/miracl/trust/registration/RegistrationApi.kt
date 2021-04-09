package com.miracl.trust.registration

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.network.*
import com.miracl.trust.util.json.JsonUtil

private const val DEFAULT_PIN_LENGTH = 4

@Keep
internal data class RegisterRequestBody(
    @Expose @SerializedName("userId") val userId: String,
    @Expose @SerializedName("deviceName") val deviceName: String,
    @Expose @SerializedName("activateCode") val activationToken: String,
    @Expose @SerializedName("pushToken") val pushToken: String? = null,
)

@Keep
internal data class RegisterResponse(
    @Expose @SerializedName("mpinId") val mpinId: String,
    @Expose @SerializedName("regOTT") val regOTT: String,
    @Expose @SerializedName("pinLength") val pinLength: Int = DEFAULT_PIN_LENGTH
)

@Keep
internal data class SignatureResponse(
    @Expose @SerializedName("clientSecretShare") var clientSecretShare: String,
    @Expose @SerializedName("cs2url") val clientSecret2Url: String,
    @Expose @SerializedName("dtas") val dtas: String,
    @Expose @SerializedName("curve") val curve: String
)

@Keep
internal data class ClientSecretShare2Response(
    @Expose @SerializedName("clientSecret") var clientSecretShare2: String,
    @Expose @SerializedName("createdAt") val createdAt: Int,
    @Expose @SerializedName("message") val message: String,
    @Expose @SerializedName("version") val version: String
)

internal interface RegistrationApi {
    suspend fun executeRegisterRequest(registerRequestBody: RegisterRequestBody): MiraclResult<RegisterResponse, Error>

    suspend fun executeSignatureRequest(registerResponse: RegisterResponse): MiraclResult<SignatureResponse, Error>

    suspend fun executeClientSecretRequest(clientSecretUrl: String): MiraclResult<ClientSecretShare2Response, Error>
}

internal class RegistrationApiManager(
    private val httpRequestExecutor: HttpRequestExecutor,
    private val projectId: String,
    private val jsonUtil: JsonUtil,
    private val apiSettings: ApiSettings
) : ApiManager(), RegistrationApi {
    override suspend fun executeRegisterRequest(registerRequestBody: RegisterRequestBody): MiraclResult<RegisterResponse, Error> {
        try {
            val registerRequestAsJson =
                jsonUtil.toJson(registerRequestBody)
            val registerRequest =
                ApiRequest(
                    method = HttpMethod.PUT,
                    headers = getRpsRequestHeaders(projectId),
                    body = registerRequestAsJson,
                    params = null,
                    url = apiSettings.registerUrl
                )

            val result = httpRequestExecutor.execute(registerRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val registerResponse =
                jsonUtil.fromJsonString((result as MiraclSuccess).value, RegisterResponse::class)

            return MiraclSuccess(registerResponse)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(ex.message ?: RegistrationResponses.FAIL.message),
                exception = ex
            )
        }
    }

    override suspend fun executeSignatureRequest(registerResponse: RegisterResponse): MiraclResult<SignatureResponse, Error> {
        try {
            val signatureParams = mutableMapOf(
                "regOTT" to registerResponse.regOTT
            )
            val signatureRequest = ApiRequest(
                method = HttpMethod.GET,
                headers = getRpsRequestHeaders(projectId),
                body = null,
                params = signatureParams,
                url = "${apiSettings.signatureUrl}/${registerResponse.mpinId}"
            )

            val result = httpRequestExecutor.execute(signatureRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val signatureResponse =
                jsonUtil.fromJsonString((result as MiraclSuccess).value, SignatureResponse::class)

            return MiraclSuccess(signatureResponse)
        } catch (ex: java.lang.Exception) {
            return MiraclError(
                value = Error(ex.message ?: RegistrationResponses.FAIL.message),
                exception = ex
            )
        }
    }

    override suspend fun executeClientSecretRequest(clientSecretUrl: String): MiraclResult<ClientSecretShare2Response, Error> {
        try {
            val clientSecretShare2Request = ApiRequest(
                method = HttpMethod.GET,
                headers = getRpsRequestHeaders(projectId),
                body = null,
                params = null,
                url = clientSecretUrl
            )

            val result = httpRequestExecutor.execute(clientSecretShare2Request)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val clientSecretShare2Response =
                jsonUtil.fromJsonString(
                    (result as MiraclSuccess).value,
                    ClientSecretShare2Response::class
                )

            return MiraclSuccess(clientSecretShare2Response)
        } catch (ex: java.lang.Exception) {
            return MiraclError(
                value = Error(ex.message ?: RegistrationResponses.FAIL.message),
                exception = ex
            )
        }
    }
}
