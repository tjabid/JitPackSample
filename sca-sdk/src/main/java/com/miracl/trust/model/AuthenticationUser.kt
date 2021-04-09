package com.miracl.trust.model

import androidx.annotation.Keep

/**
 * Object representing the authentication user in the platform.
 * @param identity Identity used for authentication.
 */
@Keep
data class AuthenticationUser(
    val identity: Identity
)
