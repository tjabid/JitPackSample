package com.miracl.trust.network

/***
 * HttpMethod is a MIRACLTrust SDK representation of the HTTP methods
 */
enum class HttpMethod(val method: String) {
    GET("GET"),
    POST("POST"),
    PUT("PUT")
}

/***
 * MiraclApiRequest is a data class that keeps the main properties of a HTTP request.
 */
data class ApiRequest(
    val method: HttpMethod,
    val headers: Map<String, String>?,
    val body: String?,
    val params: Map<String, String>?,
    val url: String
)
