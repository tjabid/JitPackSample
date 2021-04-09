package com.miracl.trust.storage.security

import android.content.SharedPreferences
import android.util.AndroidException

internal class KeyProvider(
    private val storagePreferences: SharedPreferences,
    private val keyProtector: KeyProtector
) {
    companion object {
        private const val STORAGE_KEY_ID = "storage_key"

        private const val ERROR_UNABLE_TO_WRITE_KEY =
            "Unable to save the DB key into the preferences file."
        private const val ERROR_UNABLE_TO_READ_KEY =
            "Unable to get the DB key from the preferences file."
    }

    private val hasStorageKey: Boolean
        get() = storagePreferences.contains(STORAGE_KEY_ID)

    val storageKey: ByteArray
        get() {
            if (!hasStorageKey) {
                val encryptedStorageKey = keyProtector.createStorageKey()

                val keySaved = storagePreferences.edit()
                    .putString(STORAGE_KEY_ID, encryptedStorageKey.encodeToString())
                    .commit()

                return if (keySaved) {
                    keyProtector.decryptStorageKey(getKeyFromSharedPreference())
                } else
                    throw AndroidException(ERROR_UNABLE_TO_WRITE_KEY)
            }

            return keyProtector.decryptStorageKey(getKeyFromSharedPreference())
        }

    private fun getKeyFromSharedPreference() =
        storagePreferences
            .getString(STORAGE_KEY_ID, null)?.decodeToByteArray()
            ?: throw NoSuchElementException(ERROR_UNABLE_TO_READ_KEY)

    private fun String.decodeToByteArray(): ByteArray {
        return this.toByteArray(Charsets.ISO_8859_1)
    }

    private fun ByteArray.encodeToString(): String {
        return String(this, Charsets.ISO_8859_1)
    }
}