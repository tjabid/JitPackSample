package tg.sdk.sca.data.common.network.interceptor

import okhttp3.Interceptor
import okhttp3.Response
import tg.sdk.sca.BuildConfig

class ScaCommonHeadersInterceptor() : Interceptor {

    private val PARAM_CONTENT_TYPE = "Content-Type"
    private val CONTENT_TYPE_JSON = "application/json"

    private val BANK_ID = "X-TG-BankId"

    override fun intercept(chain: Interceptor.Chain): Response {
        val newRequest = chain.request().newBuilder()
            .addHeader(PARAM_CONTENT_TYPE, CONTENT_TYPE_JSON)
            .addHeader(BANK_ID, BuildConfig.HEADER_BANK_ID)
            .build()
        return chain.proceed(newRequest)
    }
}