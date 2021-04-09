package com.miracl.trust.signing

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.network.*
import com.miracl.trust.util.json.JsonUtil

@Keep
internal data class DVSClientSecretRequestBody(
    @Expose val publicKey: String,
    @Expose val deviceName: String,
    @Expose val dvsRegisterToken: String
)

@Keep
internal data class DVSClientSecret1Response(
    @Expose val dvsClientSecretShare: String,
    @Expose @SerializedName("cs2url") val cs2Url: String,
    @Expose val curve: String,
    @Expose val dtas: String,
    @Expose val mpinId: String
)

@Keep
internal data class DVSClientSecret2Response(
    @Expose val dvsClientSecret: String
)

@Keep
internal interface SigningRegistrationApi {
    suspend fun executeDVSClientSecret1Request(
        publicKey: String,
        dvsRegistrationToken: String,
        deviceName: String
    ): MiraclResult<DVSClientSecret1Response, Error>

    suspend fun executeDVSClientSecret2Request(url: String): MiraclResult<DVSClientSecret2Response, Error>
}

internal class SigningRegistrationApiManager(
    private val jsonUtil: JsonUtil,
    private val projectId: String,
    private val httpRequestExecutor: HttpRequestExecutor,
    private val apiSettings: ApiSettings
) : ApiManager(), SigningRegistrationApi {
    override suspend fun executeDVSClientSecret1Request(
        publicKey: String,
        dvsRegistrationToken: String,
        deviceName: String
    ): MiraclResult<DVSClientSecret1Response, Error> {
        val requestBody = DVSClientSecretRequestBody(
            publicKey,
            deviceName,
            dvsRegistrationToken
        )

        try {
            val requestBodyJson = jsonUtil.toJson(requestBody)
            val apiRequest = ApiRequest(
                method = HttpMethod.POST,
                headers = getRpsRequestHeaders(projectId),
                body = requestBodyJson,
                params = null,
                url = apiSettings.dvsRegUrl
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val dvsClientSecret1Response =
                jsonUtil.fromJsonString(
                    (result as MiraclSuccess).value,
                    DVSClientSecret1Response::class
                )

            return MiraclSuccess(dvsClientSecret1Response)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(
                    ex.message
                        ?: SigningRegistrationErrorResponses.INVALID_DVS_CLIENT_1_SECRET_RESPONSE.message
                ),
                exception = ex
            )
        }
    }

    override suspend fun executeDVSClientSecret2Request(url: String): MiraclResult<DVSClientSecret2Response, Error> {
        try {
            val apiRequest = ApiRequest(
                method = HttpMethod.GET,
                headers = null,
                body = null,
                params = null,
                url = url
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val dvsClientSecret2Response =
                jsonUtil.fromJsonString(
                    (result as MiraclSuccess).value,
                    DVSClientSecret2Response::class
                )

            return MiraclSuccess(dvsClientSecret2Response)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(
                    ex.message
                        ?: SigningRegistrationErrorResponses.INVALID_DVS_CLIENT_2_SECRET_RESPONSE.message
                ),
                exception = ex
            )
        }
    }
}