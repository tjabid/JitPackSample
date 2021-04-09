package tg.sdk.sca.data.common.network.interceptor

import okhttp3.*
import okio.Buffer
import timber.log.Timber
import java.nio.charset.Charset

class CurlLoggingInterceptor(
    private val curlOptions: String? = null
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request: Request = chain.request()
        var compressed = false

        var curlCmd = "curl"

        if (curlOptions != null) {
            curlCmd += " $curlOptions"
        }
        curlCmd += " -v -k -X ${request.method}"

        request.headers.forEach {
            val name = it.first
            val value = it.second
            if ("Accept-Encoding".equals(name, ignoreCase = true) && "gzip".equals(
                    value,
                    ignoreCase = true
                )
            ) {
                compressed = true
            }
            curlCmd += " -H \"$name: $value\""
        }

        val requestBody: RequestBody? = request.body
        if (requestBody != null) {
            val buffer = Buffer()
            requestBody.writeTo(buffer)
            var charset: Charset = Charsets.UTF_8
            val contentType: MediaType? = requestBody.contentType()
            if (contentType != null) {
                contentType.charset(Charsets.UTF_8)?.let {
                    charset = it
                }
            }
            // try to keep to a single line and use a subshell to preserve any line breaks
            curlCmd += " --data $'" + buffer.readString(charset).replace("\n", "\\n")
                .toString() + "'"
        }

        curlCmd += (if (compressed) " --compressed " else " ") + request.url
        Timber.tag("RequestCurl").d(curlCmd)
        return chain.proceed(request)
    }
}