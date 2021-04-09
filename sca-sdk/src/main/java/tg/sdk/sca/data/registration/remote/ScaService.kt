package tg.sdk.sca.data.registration.remote

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.HTTP
import retrofit2.http.POST
import tg.sdk.sca.data.consent.*
import tg.sdk.sca.data.registration.model.ActivationToken

private const val INITIATE_PATH = "/sca/v1/enrol"
private const val CONSENT_PATH = "/sca/v1/consent"
private const val DEREGISTER_PATH = "/sca/v1/revoke"
private const val RETRIEVE_CONSENT_PATH = "$CONSENT_PATH/retrieve"
private const val LIST_CONSENT_PATH = "$CONSENT_PATH/list"

interface ScaService {

    /**
     * Get activation token required for registration
     */
    @POST(INITIATE_PATH)
    fun fetchActivationToken(
        @Body request: HashMap<String, String>,
    ): Call<ActivationToken>

    /**
     * Get consent based on session id
     */
    @POST(RETRIEVE_CONSENT_PATH)
    fun getConsent(
        @Body request: RetrieveConsentRequest,
    ): Call<TgBobfConsent>

    /**
     * Get consent based on session id
     */
    @POST(CONSENT_PATH)
    fun updateConsent(
        @Body request: UpdateConsentRequest,
    ): Call<Unit>

    /**
     * Get list of consent based on Consent Type
     */
    @POST(LIST_CONSENT_PATH)
    fun getConsentList(
        @Body request: ListConsentRequest,
    ): Call<List<TgBobfConsent>>

    /**
     * Revoke consent
     */
    @HTTP(method = "DELETE", path = CONSENT_PATH, hasBody = true)
    fun revokeConsent(
        @Body request: RevokeConsentRequest,
    ): Call<Unit>

    /**
     * deregister
     */
    @POST(DEREGISTER_PATH)
    fun deregister(
        @Body request: DeregisterRequest,
    ): Call<Unit>

}