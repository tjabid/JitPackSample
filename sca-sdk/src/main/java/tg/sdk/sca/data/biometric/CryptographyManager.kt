package tg.sdk.sca.data.biometric

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import androidx.annotation.RequiresApi
import com.google.gson.Gson
import com.miracl.trust.storage.security.KeyProtector
import tg.sdk.sca.data.biometric.BiometricPromptUtils.SHARED_PREFS_FILENAME
import timber.log.Timber
import java.nio.charset.Charset
import java.security.KeyStore
import java.security.KeyStoreException
import java.security.UnrecoverableKeyException
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * Handles encryption and decryption
 */
interface CryptographyManager {

    fun getInitializedCipherForEncryption(onFailure: (String) -> Unit): Cipher?

    fun getInitializedCipherForDecryption(onFailure: (String) -> Unit): Cipher?

    /**
     * The Cipher created with [getInitializedCipherForEncryption] is used here
     */
    fun encryptData(plaintext: String, cipher: Cipher): CipherTextWrapper

    /**
     * The Cipher created with [getInitializedCipherForDecryption] is used here
     */
    @Throws(KeyStoreException::class)
    fun decryptData(/*ciphertext: ByteArray, */cipher: Cipher): String

    fun persistCiphertextWrapperToSharedPrefs(
        token: String,
        cipher: Cipher,
        context: Context
    )

    fun getCiphertextWrapperFromSharedPrefs(
        context: Context
    ): CipherTextWrapper?

}

fun CryptographyManager(context: Context): CryptographyManager = CryptographyManagerImpl(context)

/**
 * To get an instance of this private CryptographyManagerImpl class, use the top-level function
 * fun CryptographyManager(): CryptographyManager = CryptographyManagerImpl()
 */
private class CryptographyManagerImpl(private val context: Context) : CryptographyManager {

    private val KEY_SIZE = 256
    private val ANDROID_KEYSTORE = "AndroidKeyStore"
    private val SCA_SDK_KEYSTORE = "biometric_sca_sdk_encryption_key"
    private val ENCRYPTION_BLOCK_MODE = KeyProperties.BLOCK_MODE_GCM
    private val ENCRYPTION_PADDING = KeyProperties.ENCRYPTION_PADDING_NONE
    private val ENCRYPTION_ALGORITHM = KeyProperties.KEY_ALGORITHM_AES

    private var cipherTextWrapper: CipherTextWrapper? = getCiphertextWrapperFromSharedPrefs(context)

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getInitializedCipherForEncryption(onFailure: (String) -> Unit): Cipher? {
        val cipher = getCipher()
        val secretKey = getOrCreateSecretKey(onFailure = onFailure)
        return secretKey?.let {
            try {
                cipher.init(Cipher.ENCRYPT_MODE, secretKey)
                cipher
            } catch (e: Exception) {
                Timber.e(e)
                onFailure.invoke("Failed to verify biometrics authentication")
                null
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun getInitializedCipherForDecryption(onFailure: (String) -> Unit): Cipher? {
        try {
            val cipher = getCipher()
            val secretKey = getSecretKey(onFailure = onFailure)
            if (cipherTextWrapper == null || cipherTextWrapper?.initializationVector == null) {
                onFailure.invoke("Failed to Authorize!")
                return null
            }
            return secretKey?.let {
                cipher.init(
                    Cipher.DECRYPT_MODE,
                    secretKey,
                    GCMParameterSpec(128, cipherTextWrapper?.initializationVector)
                )
                cipher
            }
        } catch (e: Exception) {
            Timber.e(e)
            onFailure.invoke("Biometric has changed, failed to verify biometrics authentication")
            return null
        }
    }

    @Throws(KeyStoreException::class)
    override fun encryptData(plaintext: String, cipher: Cipher): CipherTextWrapper {
        val cipherText = cipher.doFinal(plaintext.toByteArray(Charset.forName("UTF-8")))
        return CipherTextWrapper(cipherText, cipher.iv)
    }

    @Throws(KeyStoreException::class)
    override fun decryptData(cipher: Cipher): String {
        return cipherTextWrapper?.let {
            val plaintext = cipher.doFinal(it.ciphertext)
            String(plaintext, Charset.forName("UTF-8"))
        } ?: ""
    }

    private fun getCipher(): Cipher {
        val transformation = "$ENCRYPTION_ALGORITHM/$ENCRYPTION_BLOCK_MODE/$ENCRYPTION_PADDING"
        return Cipher.getInstance(transformation)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getSecretKey(onFailure: (String) -> Unit): SecretKey? {
        try {
            // previously created Secret key for that keyName, then grab and return it.
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null) // Keystore must be loaded before it can be accessed
            val key = keyStore.getKey(SCA_SDK_KEYSTORE, null)
            return if (key == null) {
                //todo re-verification flow
                onFailure.invoke("Failed to verify biometrics authentication")
                null
            } else {
                key as SecretKey
            }
        } catch (e: Exception) {
            Timber.e(e)
            onFailure.invoke(e.message ?: "Failed to verify biometrics authentication")
        }
        return null
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun getOrCreateSecretKey(onFailure: (String) -> Unit): SecretKey? {
        try {
            // If Secretkey was previously created for that keyName, then delete it and create new one.
            val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
            keyStore.load(null) // Keystore must be loaded before it can be accessed
            val key = keyStore.getKey(SCA_SDK_KEYSTORE, null)
            if (key != null) {
                Timber.d("key already exist")
                keyStore.deleteEntry(SCA_SDK_KEYSTORE)
            }
        } catch (e: Exception) {
            Timber.e(e)
        }

        try {
            // if you reach here, then a new SecretKey must be generated for that keyName
            val paramsBuilder = KeyGenParameterSpec.Builder(
                SCA_SDK_KEYSTORE,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
            paramsBuilder.apply {
                setBlockModes(ENCRYPTION_BLOCK_MODE)
                setEncryptionPaddings(ENCRYPTION_PADDING)
                setKeySize(KEY_SIZE)
                setUserAuthenticationRequired(true)
            }

            val keyGenParams = paramsBuilder.build()
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )
            keyGenerator.init(keyGenParams)
            return keyGenerator.generateKey()

        } catch (e: Exception) {
            Timber.e(e)
        }
        onFailure.invoke("Failed to verify biometrics authentication")
        return null
    }

    override fun persistCiphertextWrapperToSharedPrefs(
        token: String,
        cipher: Cipher,
        context: Context
    ) {
        val cipherTextWrapper = encryptData(token, cipher)
        val json = Gson().toJson(cipherTextWrapper)
        context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE).edit()
            .putString(BiometricPromptUtils.CIPHERTEXT_WRAPPER, json).apply()
    }

    override fun getCiphertextWrapperFromSharedPrefs(
        context: Context
    ): CipherTextWrapper? {
        val json = context.getSharedPreferences(SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)
            .getString(BiometricPromptUtils.CIPHERTEXT_WRAPPER, null)
        cipherTextWrapper = Gson().fromJson(json, CipherTextWrapper::class.java)
        return cipherTextWrapper
    }
}


data class CipherTextWrapper(val ciphertext: ByteArray, val initializationVector: ByteArray)