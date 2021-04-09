package com.miracl.trust.network

import com.miracl.trust.MiraclResult

/***
 * HttpRequestExecutor is an interface providing pluggable networking layer
 * of the MIRACLTrust SDK. If implemented and passed as an argument when initializing the
 * MIRACLTrust SDK, you can provide your own HTTP request executor.
 *
 * */
interface HttpRequestExecutor {

    /***
     * executes HTTP requests.
     * @param apiRequest provides the required information for
     * processing the HTTP request.
     * @return MiraclResult<String, Error> which can be either
     * MiraclSuccess with value of type String (the response of the executed request) or
     * MiraclError with value of type java.lang.Error with a message.
     **/
    suspend fun execute(apiRequest: ApiRequest): MiraclResult<String, Error>
}
