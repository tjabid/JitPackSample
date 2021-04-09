package com.miracl.trust.configuration

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.network.*
import com.miracl.trust.util.json.JsonUtil
import java.net.URL

@Keep
internal data class ClientSettings(
    @Expose @SerializedName("signatureURL") val signatureUrl: String,
    @Expose @SerializedName("registerURL") val registerUrl: String,
    @Expose @SerializedName("authenticateURL") val authenticateUrl: String,
    @Expose @SerializedName("pass1URL") val pass1Url: String,
    @Expose @SerializedName("pass2URL") val pass2Url: String,
    @Expose @SerializedName("dvsRegURL") val dvsRegUrl: String,
    @Expose @SerializedName("verificationURL") val verificationUrl: String
)

internal interface ConfigurationApi {
    val projectId: String
    suspend fun getClientSettings(): MiraclResult<String, Error>
}

internal class ConfigurationApiManager(
    val httpRequestExecutor: HttpRequestExecutor,
    override val projectId: String,
    val jsonUtil: JsonUtil,
    private val apiSettings: ApiSettings
) : ApiManager(), ConfigurationApi {

    override suspend fun getClientSettings(): MiraclResult<String, Error> {
        try {
            val request =
                ApiRequest(
                    method = HttpMethod.GET,
                    headers = null,
                    body = null,
                    params = null,
                    url = apiSettings.clientSettingsUrl
                )

            val result = httpRequestExecutor.execute(request)
            if (result is MiraclError) {
                return MiraclError(value = Error(result.value.message))
            }

            result as MiraclSuccess

            val clientSettingsResponse =
                jsonUtil.fromJsonString(result.value, ClientSettings::class)

            if (!isValidUrl(clientSettingsResponse.signatureUrl)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.FAIL_SIGNATURE_URL.message)
                )
            }

            apiSettings.signatureUrl = clientSettingsResponse.signatureUrl

            if (!isValidUrl(clientSettingsResponse.registerUrl)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.FAIL_REGISTER_URL.message)
                )
            }

            apiSettings.registerUrl = clientSettingsResponse.registerUrl

            if (!isValidUrl(clientSettingsResponse.authenticateUrl)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.FAIL_AUTH_URL.message)
                )
            }

            apiSettings.authenticateUrl = clientSettingsResponse.authenticateUrl

            if (!isValidUrl(clientSettingsResponse.pass1Url)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.FAIL_PASS1_URL.message)
                )
            }

            apiSettings.pass1Url = clientSettingsResponse.pass1Url

            if (!isValidUrl(clientSettingsResponse.pass2Url)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.FAIL_PASS2_URL.message)
                )
            }

            apiSettings.pass2Url = clientSettingsResponse.pass2Url

            if (!isValidUrl(clientSettingsResponse.dvsRegUrl)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.DVS_REG_URL.message)
                )
            }

            apiSettings.dvsRegUrl = clientSettingsResponse.dvsRegUrl

            if (!isValidUrl(clientSettingsResponse.verificationUrl)) {
                return MiraclError(
                    value = Error(ConfigurationResponses.VERIFICATION_URL.message)
                )
            }

            apiSettings.verificationUrl = clientSettingsResponse.verificationUrl

            return MiraclSuccess(
                value = ConfigurationResponses.SUCCESS.message
            )
        } catch (ex: Exception) {
            return MiraclError(
                value = Error(
                    ex.message
                        ?: ConfigurationResponses.FAIL.message
                ), exception = ex
            )
        }
    }

    private fun isValidUrl(url: String): Boolean {
        if (url.isBlank()) {
            return false
        }

        return try {
            val validUrl = URL(url).toURI()
            validUrl != null
        } catch (ex: java.lang.Exception) {
            false
        }
    }
}
