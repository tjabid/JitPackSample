package tg.sdk.sca.data.registration.model

import androidx.annotation.Keep

@Keep
data class ActivationToken (
    val actToken: String,
    val expireTime: Long,
    val userId: String
)