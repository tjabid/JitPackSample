package tg.sdk.sca.data.common.network

import com.google.gson.annotations.SerializedName

data class ResponseContent<T>(
    @SerializedName("errorMessage")
    val message: String,
    @SerializedName("errorCode")
    val operationCode: String,
    @SerializedName("resultObject")
    val resultObject: T
)