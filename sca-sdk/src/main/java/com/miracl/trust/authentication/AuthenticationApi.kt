package com.miracl.trust.authentication

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.network.*
import com.miracl.trust.util.json.JsonUtil

@Keep
internal data class AuthenticateRequestBody(
    @Expose @SerializedName("authOTT") val authOtt: String
)

@Keep
internal data class SigningRegister(
    @Expose @SerializedName("token") val token: String?,
    @Expose @SerializedName("curve") val curve: String?
)

@Keep
internal data class AuthenticateResponse(
    @Expose @SerializedName("status") val status: Int,
    @Expose @SerializedName("message") val message: String,
    @Expose @SerializedName("dvsRegister") val dvsRegister: SigningRegister?,
    @Expose @SerializedName("code") val code: String?
)

@Keep
internal data class Pass1Response(@Expose @SerializedName("y") val Y: String)

@Keep
internal data class Pass2RequestBody(
    @Expose @SerializedName("mpin_id") val mpinId: String,
    @Expose @SerializedName("WID") val accessId: String?,
    @Expose @SerializedName("V") val V: String
)

@Keep
internal data class Pass2Response(@Expose @SerializedName("authOTT") val authOtt: String)

@Keep
internal interface AuthenticationApi {
    suspend fun executePass1Request(pass1RequestBody: Pass1RequestBody): MiraclResult<Pass1Response, Error>

    suspend fun executePass2Request(pass2RequestBody: Pass2RequestBody): MiraclResult<Pass2Response, Error>

    suspend fun executeAuthenticateRequest(authenticationRequestBody: AuthenticateRequestBody): MiraclResult<AuthenticateResponse, Error>
}

internal class AuthenticationApiManager(
    private val httpRequestExecutor: HttpRequestExecutor,
    private val projectId: String,
    private val jsonUtil: JsonUtil,
    private val apiSettings: ApiSettings,
) : ApiManager(), AuthenticationApi {
    override suspend fun executePass1Request(pass1RequestBody: Pass1RequestBody): MiraclResult<Pass1Response, Error> {
        try {
            val pass1RequestBodyAsJson = jsonUtil.toJson(pass1RequestBody)
            val apiRequest = ApiRequest(
                method = HttpMethod.POST,
                headers = getRpsRequestHeaders(projectId),
                body = pass1RequestBodyAsJson,
                params = null,
                url = apiSettings.pass1Url
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val pass1Response =
                jsonUtil.fromJsonString((result as MiraclSuccess).value, Pass1Response::class)

            return MiraclSuccess(pass1Response)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(ex.message ?: AuthenticationResponses.FAIL_INVALID_RESPONSE.message),
                exception = ex
            )
        }
    }

    override suspend fun executePass2Request(pass2RequestBody: Pass2RequestBody): MiraclResult<Pass2Response, Error> {
        try {
            val pass2RequestBodyAsJson = jsonUtil.toJson(pass2RequestBody)
            val apiRequest = ApiRequest(
                method = HttpMethod.POST,
                headers = getRpsRequestHeaders(projectId),
                body = pass2RequestBodyAsJson,
                params = null,
                url = apiSettings.pass2Url
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val pass2Response =
                jsonUtil.fromJsonString((result as MiraclSuccess).value, Pass2Response::class)

            return MiraclSuccess(pass2Response)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(ex.message ?: AuthenticationResponses.FAIL.message),
                exception = ex
            )
        }
    }

    override suspend fun executeAuthenticateRequest(authenticationRequestBody: AuthenticateRequestBody): MiraclResult<AuthenticateResponse, Error> {
        try {
            val authenticateRequestBodyAsJson = jsonUtil.toJson(authenticationRequestBody)
            val apiRequest = ApiRequest(
                method = HttpMethod.POST,
                headers = getRpsRequestHeaders(projectId),
                body = authenticateRequestBodyAsJson,
                params = null,
                url = apiSettings.authenticateUrl
            )

            val result = httpRequestExecutor.execute(apiRequest)
            if (result is MiraclError) {
                return MiraclError(result.value)
            }

            val authenticateResponse =
                jsonUtil.fromJsonString(
                    (result as MiraclSuccess).value,
                    AuthenticateResponse::class
                )

            return MiraclSuccess(authenticateResponse)
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(ex.message ?: AuthenticationResponses.FAIL.message),
                exception = ex
            )
        }
    }
}
