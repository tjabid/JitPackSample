package com.miracl.trust.registration

import androidx.annotation.Keep

@Keep
data class ActivationTokenResponse(
    val userId: String,
    val activationToken: ActivationToken
)
