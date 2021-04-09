package com.miracl.trust.network

import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants.NETWORK_REQUEST
import com.miracl.trust.util.log.LoggerConstants.NETWORK_RESPONSE
import com.miracl.trust.util.log.LoggerConstants.NETWORK_TAG
import okhttp3.*
import okhttp3.HttpUrl.Companion.toHttpUrlOrNull
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import tg.sdk.sca.data.common.network.interceptor.CurlLoggingInterceptor
import java.io.IOException
import java.net.URL

internal enum class HttpRequestExecutorResponses(val message: String) {
    EXECUTION_ERROR("Error while executing http request: "),
    NOT_FOUND_ERROR("Error: The server responded with 404 error code."),
    NOT_ACCEPTABLE_ERROR("Error: Server responded with 406 - 'Not acceptable'"),
    NOT_AUTHORIZED_ERROR("Error: Server responded with 401 - 'Unauthorized'"),
    TYPE_NOT_SUPPORTED_ERROR("Error: Server responded with 400 - Response type not supported / Unexpected error occurred"),
    SERVER_ERROR("Error: Server responded with code 500 - Server error"),
    TIMEOUT_ERROR("Error: Request Timeout with code 408"),
    SUCCESS("Success: The server responded with code 200.")
}

/**
 * Provides implementation of the HttpRequestExecutor that uses OkHttp client.
 */
internal class OkHttpRequestExecutor : HttpRequestExecutor, Loggable {
    companion object {
        private const val HTTP_METHOD_NOT_EXPECTED_TO_HAVE_A_BODY_LOG =
            "This HttpMethod is not expected to have a body."
    }

    private val jsonMediaType: MediaType? = "application/json; charset=utf-8".toMediaType()
    private var client: OkHttpClient

    init {
        val builder = OkHttpClient.Builder()
            .addInterceptor(object : Interceptor {
                override fun intercept(chain: Interceptor.Chain): Response {
                    miraclLogger?.debug(
                        NETWORK_TAG,
                        NETWORK_REQUEST.format(chain.request().method, chain.request().url)
                    )

                    val response: Response
                    try {
                        response = chain.proceed(chain.request())
                    } catch (ex: IOException) {
                        miraclLogger?.error(NETWORK_TAG, ex.toString())
                        throw ex
                    }

                    miraclLogger?.debug(
                        NETWORK_TAG,
                        NETWORK_RESPONSE.format(
                            chain.request().method,
                            chain.request().url,
                            response.code
                        )
                    )
                    return response
                }
            })
            .addInterceptor(CurlLoggingInterceptor())
        client = builder.build()
    }

    /**
     * Implementation of the execute(apiRequest) function of the HttpRequestExecutor.
     * @param apiRequest is a MiraclTrust class that provides the needed data for
     * a http request to be executed.
     * @return MiraclResult<String, Error>
     *      - If the result is success execute returns MiraclSuccess
     *      with a string value of the received response.
     *      - If the result is error execute returns the error with a message.
     *      If an exception is thrown, the error passes the exception as an object.
     */
    override suspend fun execute(apiRequest: ApiRequest): MiraclResult<String, Error> {
        try {
            val request = buildOkHttpRequest(apiRequest)
            val response: Response = client.newCall(request).execute()

            return when (response.code) {
                200 -> MiraclSuccess(
                    response.body?.string() ?: HttpRequestExecutorResponses.SUCCESS.message
                )
                404 -> MiraclError(Error(HttpRequestExecutorResponses.NOT_FOUND_ERROR.message))
                401 -> MiraclError(Error(HttpRequestExecutorResponses.NOT_AUTHORIZED_ERROR.message))
                400 -> MiraclError(Error(HttpRequestExecutorResponses.TYPE_NOT_SUPPORTED_ERROR.message))
                406 -> MiraclError(Error(HttpRequestExecutorResponses.NOT_ACCEPTABLE_ERROR.message))
                408 -> MiraclError(Error(HttpRequestExecutorResponses.TIMEOUT_ERROR.message))
                500 -> MiraclError(Error(HttpRequestExecutorResponses.SERVER_ERROR.message))
                else -> MiraclError(Error("${HttpRequestExecutorResponses.EXECUTION_ERROR.message} $response"))
            }
        } catch (ex: Exception) {
            return MiraclError(
                value = Error("${HttpRequestExecutorResponses.EXECUTION_ERROR.message}${ex.message}: $ex"),
                exception = ex
            )
        }
    }

    private fun getOkHttpHeaders(headers: Map<String, String>?): Headers {
        val headersBuilder: Headers.Builder = Headers.Builder()

        if (headers != null && headers.isNotEmpty()) {
            for ((key, value) in headers) {
                headersBuilder.add(key, value)
            }
        }

        return headersBuilder.build()
    }

    private fun buildOkHttpRequest(apiRequest: ApiRequest): Request {
        val url: HttpUrl? = buildOkHttpUrl(apiRequest) ?: apiRequest.url.toHttpUrlOrNull()

        val headers = getOkHttpHeaders(apiRequest.headers)
        val okHttpRequestBuilder: Request.Builder = Request.Builder()
            .url(url!!)
            .headers(headers)

        apiRequest.body?.also { body ->
            when (apiRequest.method) {
                HttpMethod.POST -> {
                    okHttpRequestBuilder.post(
                        body.toRequestBody(jsonMediaType)
                    )
                }
                HttpMethod.PUT -> {
                    okHttpRequestBuilder.put(
                        body.toRequestBody(jsonMediaType)
                    )
                }
                else -> {
                    miraclLogger?.info(NETWORK_TAG, HTTP_METHOD_NOT_EXPECTED_TO_HAVE_A_BODY_LOG)
                }
            }
        }

        return okHttpRequestBuilder.build()
    }

    private fun buildOkHttpUrl(apiRequest: ApiRequest): HttpUrl? {
        if (apiRequest.params.isNullOrEmpty()) {
            return null
        }

        val urlBuilder: HttpUrl.Builder = HttpUrl.Builder()
        urlBuilder
            .scheme(URL(apiRequest.url).protocol)
            .host(URL(apiRequest.url).host)
            .addPathSegment(URL(apiRequest.url).path)

        for ((key, value) in apiRequest.params) {
            urlBuilder.addEncodedQueryParameter(encodedName = key, encodedValue = value)
            urlBuilder.build()
        }

        return urlBuilder.build()
    }

    fun addClientInterceptor(interceptor: Interceptor) {
        val interceptedClient = client.newBuilder().addInterceptor(interceptor).build()
        client = interceptedClient
    }
}
