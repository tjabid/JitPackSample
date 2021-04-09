package com.miracl.trust.model

import androidx.annotation.Keep

/**
 * Identity is a Miracl Trust data class to represent an identity.
 * @param userId Identifier of the identity. Could be email, username, etc.
 * @param isBlocked provides information if the identity is blocked or not.
 * @param mpinId representing the identity to the Miracl Trust Platform.
 * @param token representing the identity securely.
 * @param dtas required for server side validation.
 */
@Keep
data class Identity(
    val userId: String,
    val pinLength: Int,
    val isBlocked: Boolean,
    val mpinId: ByteArray,
    val token: ByteArray,
    val dtas: String
) {
    companion object {
        private const val AUTOGENERATED_MULTIPLIER = 31
    }

    // AUTOGENERATED
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as Identity

        if (userId != other.userId) return false
        if (pinLength != other.pinLength) return false
        if (isBlocked != other.isBlocked) return false
        if (!mpinId.contentEquals(other.mpinId)) return false
        if (!token.contentEquals(other.token)) return false
        if (dtas != other.dtas) return false

        return true
    }

    // AUTOGENERATED
    override fun hashCode(): Int {
        var result = userId.hashCode()
        result = AUTOGENERATED_MULTIPLIER * result + pinLength.hashCode()
        result = AUTOGENERATED_MULTIPLIER * result + isBlocked.hashCode()
        result = AUTOGENERATED_MULTIPLIER * result + mpinId.hashCode()
        result = AUTOGENERATED_MULTIPLIER * result + token.contentHashCode()
        result = AUTOGENERATED_MULTIPLIER * result + dtas.hashCode()
        return result
    }
}

internal fun Identity.isEmpty(): Boolean = dtas.isBlank() || mpinId.isEmpty() || token.isEmpty()
