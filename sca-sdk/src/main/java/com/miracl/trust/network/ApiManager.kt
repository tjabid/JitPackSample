package com.miracl.trust.network

internal abstract class ApiManager {
    companion object {
        private const val MIRACL_CID_HEADER_KEY = "X-MIRACL-CID"
    }

    open fun getRpsRequestHeaders(projectId: String): Map<String, String> {
        return mapOf(
            MIRACL_CID_HEADER_KEY to projectId
        )
    }
}

internal fun String.appendPath(path: String): String {
    val urlDelimiter = '/'

    return this.trimEnd(urlDelimiter) + urlDelimiter + path.trimStart(urlDelimiter)
}
