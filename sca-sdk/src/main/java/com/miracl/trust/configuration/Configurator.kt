package com.miracl.trust.configuration

import com.miracl.trust.MiraclError
import org.json.JSONException

internal class ConfigurationFailException(message: String) : Exception(message)

internal enum class ConfigurationResponses(val message: String) {
    SUCCESS("Configuration is complete"),
    FAIL("Configuration fail"),
    FAIL_REGISTER_URL("Configuration fail: registerUrl missing"),
    FAIL_SIGNATURE_URL("Configuration fail: signatureUrl missing"),
    FAIL_AUTH_URL("Configuration fail: authenticateURL missing"),
    FAIL_PASS1_URL("Configuration fail: pass1Url missing"),
    FAIL_PASS2_URL("Configuration fail: pass2Url missing"),
    DVS_REG_URL("Configuration fail: dvsRegURL missing"),
    VERIFICATION_URL("Configuration fail: verificationURL missing"),
    FAIL_PROJECT_ID("projectId is not correctly set."),
    FAIL_PROJECT_ID_UNKNOWN("projectId is unknown"),
    FAIL_INVALID_RESPONSE("Configuration fail because of invalid server response. Please contact MIRACL support."),
}

internal interface ConfiguratorContract {
    suspend fun configure()
}

internal class Configurator(private val configurationApi: ConfigurationApi) : ConfiguratorContract {
    override suspend fun configure() {
        validateConfiguration(configurationApi.projectId)
        val clientSettingsResponse = configurationApi.getClientSettings()
        if (clientSettingsResponse is MiraclError) {
            when {
                isUnknownProjectIdError(clientSettingsResponse) -> {
                    throw ConfigurationFailException(
                        ConfigurationResponses.FAIL_PROJECT_ID_UNKNOWN.message
                    )
                }
                isJsonException(clientSettingsResponse) -> {
                    throw ConfigurationFailException(ConfigurationResponses.FAIL_INVALID_RESPONSE.message)
                }
                else -> {
                    throw ConfigurationFailException(
                        clientSettingsResponse.value.message
                            ?: ConfigurationResponses.FAIL.message
                    )
                }
            }
        }
    }

    private fun validateConfiguration(projectId: String) {
        if (projectId.isBlank()) {
            throw ConfigurationFailException(ConfigurationResponses.FAIL_PROJECT_ID.message)
        }
    }

    private fun <T> isJsonException(miraclResult: MiraclError<T, Error>): Boolean {
        val jsonObjectStr = "JSONObject"
        val jsonExceptionStr = "JSONException"
        return (miraclResult.value.message?.contains(jsonObjectStr) ?: false ||
                miraclResult.value.message?.contains(jsonExceptionStr) ?: false ||
                miraclResult.exception is JSONException)
    }

    private fun <T> isUnknownProjectIdError(miraclResult: MiraclError<T, Error>): Boolean =
        miraclResult.value.message?.contains("406") ?: false
}