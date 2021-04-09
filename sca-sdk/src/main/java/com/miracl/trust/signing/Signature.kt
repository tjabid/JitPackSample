package com.miracl.trust.signing

import androidx.annotation.Keep
import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

@Keep
data class Signature(
    @Expose val mpinId: String,
    @Expose @SerializedName("u") val U: String,
    @Expose @SerializedName("v") val V: String,
    @Expose val publicKey: String,
    @Expose val dtas: String,
    @Expose val hash: String
)