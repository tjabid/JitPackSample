package com.miracl.trust.crypto

import com.miracl.trust.MiraclError
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclSuccess
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.util.log.Loggable
import com.miracl.trust.util.log.LoggerConstants.CRYPTO_OPERATION_FINISHED
import com.miracl.trust.util.log.LoggerConstants.CRYPTO_OPERATION_STARTED
import com.miracl.trust.util.log.LoggerConstants.CRYPTO_TAG
import com.miracl.trust.util.secondsSince1970
import kotlinx.coroutines.sync.Semaphore
import java.util.*

internal enum class CryptoResponses(val message: String) {
    ERROR_CLIENT_TOKEN("Crypto error while getting client token."),
    ERROR_PIN_LENGTH("Required pin length does not match."),
    ERROR_PIN_CANCELED("Pin not entered."),
    ERROR_PIN("Pin code includes invalid symbols."),
    ERROR_PASS1("Crypto error while executing pass1."),
    ERROR_PASS2("Crypto error while executing pass2."),
    ERROR_SIGNING_KEY_PAIR("Crypto error while generating signing key pair"),
    ERROR_DVS_CLIENT_TOKEN("Crypto error while getting DVS client token"),
    ERROR_SIGNING("Crypto error while signing")
}

internal enum class SupportedEllipticCurves {
    BN254CX
}

internal class Crypto(
    private val cryptoExternal: CryptoExternalContract = CryptoExternal()
) : Loggable {
    companion object {
        private const val SEMAPHORE_PERMITS = 1
        private const val SEMAPHORE_ACQUIRED_PERMITS = 1
    }

    suspend fun getClientToken(
        mpinId: ByteArray,
        clientSecretShare1: ByteArray,
        clientSecretShare2: ByteArray,
        requiredPinLength: Int,
        pinProvider: PinProvider
    ): MiraclResult<ByteArray, Error> {
        val operationName = this::getClientToken.name
        logOperationStarted(operationName)

        return try {
            val clientSecret =
                cryptoExternal.combineClientSecret(clientSecretShare1, clientSecretShare2)

            var pinEntered = acquirePin(pinProvider)

            val token = pinEntered?.let { pin ->
                if (isValidPinLength(pin, requiredPinLength)) {
                    cryptoExternal.getClientToken(clientSecret, mpinId, pin.toInt())
                } else {
                    return MiraclError(
                        Error(
                            CryptoResponses.ERROR_PIN_LENGTH.message
                        )
                    )
                }
            } ?: return MiraclError(
                Error(
                    CryptoResponses.ERROR_PIN_CANCELED.message
                )
            )

            pinEntered = null

            logOperationFinished(operationName)
            MiraclSuccess(token)
        } catch (ex: NumberFormatException) {
            MiraclError(
                value = Error(
                    CryptoResponses.ERROR_PIN.message
                ), exception = ex
            )
        } catch (ex: Exception) {
            MiraclError(
                value = Error(
                    ex.message
                        ?: CryptoResponses.ERROR_CLIENT_TOKEN.message
                ), exception = ex
            )
        }
    }

    suspend fun getClientPass1Proof(
        mpinId: ByteArray,
        token: ByteArray,
        requiredPinLength: Int,
        pinProvider: PinProvider
    ): MiraclResult<Pass1Proof, Error> {
        val operationName = this::getClientPass1Proof.name
        logOperationStarted(operationName)

        return try {
            var pinEntered = acquirePin(pinProvider)

            val pass1Proof = pinEntered?.let { pin ->
                if (isValidPinLength(pin, requiredPinLength)) {
                    cryptoExternal.getClientPass1(
                        mpinId,
                        token,
                        pin.toInt()
                    )
                } else {
                    return MiraclError(
                        Error(
                            CryptoResponses.ERROR_PIN_LENGTH.message
                        )
                    )
                }
            } ?: return MiraclError(
                Error(
                    CryptoResponses.ERROR_PIN_CANCELED.message
                )
            )

            pinEntered = null

            logOperationFinished(operationName)
            MiraclSuccess(pass1Proof)
        } catch (ex: NumberFormatException) {
            MiraclError(
                value = Error(
                    CryptoResponses.ERROR_PIN.message
                ), exception = ex
            )
        } catch (ex: Exception) {
            MiraclError(
                value = Error(ex.message ?: CryptoResponses.ERROR_PASS1.message),
                exception = ex
            )
        }
    }

    fun getClientPass2Proof(
        x: ByteArray,
        y: ByteArray,
        sec: ByteArray
    ): MiraclResult<Pass2Proof, Error> {
        val operationName = this::getClientPass2Proof.name
        logOperationStarted(operationName)

        return try {
            val pass2Proof =
                cryptoExternal.getClientPass2(x, y, sec)

            logOperationFinished(operationName)
            MiraclSuccess(pass2Proof)
        } catch (ex: Exception) {
            MiraclError(
                value = Error(ex.message ?: CryptoResponses.ERROR_PASS2.message),
                exception = ex
            )
        }
    }

    fun generateSigningKeyPair(): MiraclResult<SigningKeyPair, Error> {
        val operationName = this::generateSigningKeyPair.name
        logOperationStarted(operationName)

        return try {
            val signingKeyPair = cryptoExternal.generateSigningKeyPair()

            logOperationFinished(operationName)
            MiraclSuccess(signingKeyPair)
        } catch (ex: Exception) {
            MiraclError(
                value = Error(ex.message ?: CryptoResponses.ERROR_SIGNING_KEY_PAIR.message),
                exception = ex
            )
        }
    }

    suspend fun getSigningClientToken(
        clientSecretShare1: ByteArray,
        clientSecretShare2: ByteArray,
        privateKey: ByteArray,
        signingMpinId: ByteArray,
        requiredPinLength: Int,
        pinProvider: PinProvider
    ): MiraclResult<ByteArray, Error> {
        val operationName = this::getSigningClientToken.name
        logOperationStarted(operationName)

        return try {
            val clientSecret =
                cryptoExternal.combineClientSecret(clientSecretShare1, clientSecretShare2)

            var pinEntered = acquirePin(pinProvider)

            val dvsClientToken = pinEntered?.let { pin ->
                if (isValidPinLength(pin, requiredPinLength)) {
                    cryptoExternal.getDVSClientToken(
                        clientSecret,
                        privateKey,
                        signingMpinId,
                        pin.toInt()
                    )
                } else {
                    return MiraclError(
                        Error(
                            CryptoResponses.ERROR_PIN_LENGTH.message
                        )
                    )
                }
            } ?: return MiraclError(
                Error(
                    CryptoResponses.ERROR_PIN_CANCELED.message
                )
            )

            pinEntered = null

            logOperationFinished(operationName)
            MiraclSuccess(dvsClientToken)
        } catch (ex: NumberFormatException) {
            MiraclError(
                value = Error(
                    CryptoResponses.ERROR_PIN.message
                ), exception = ex
            )
        } catch (ex: Exception) {
            MiraclError(
                value = Error(ex.message ?: CryptoResponses.ERROR_DVS_CLIENT_TOKEN.message),
                exception = ex
            )
        }
    }

    suspend fun sign(
        message: ByteArray,
        signingMpinId: ByteArray,
        signingToken: ByteArray,
        timestamp: Date,
        requiredPinLength: Int,
        pinProvider: PinProvider
    ): MiraclResult<SigningResult, Error> {
        val operationName = this::sign.name
        logOperationStarted(operationName)

        return try {
            var pinEntered = acquirePin(pinProvider)

            val signingResult = pinEntered?.let { pin ->
                if (isValidPinLength(pin, requiredPinLength)) {
                    cryptoExternal.sign(
                        message,
                        signingMpinId,
                        signingToken,
                        pin.toInt(),
                        timestamp.secondsSince1970()
                    )
                } else {
                    return MiraclError(
                        Error(
                            CryptoResponses.ERROR_PIN_LENGTH.message
                        )
                    )
                }
            } ?: return MiraclError(
                Error(
                    CryptoResponses.ERROR_PIN_CANCELED.message
                )
            )

            pinEntered = null

            logOperationFinished(operationName)
            MiraclSuccess(signingResult)
        } catch (ex: NumberFormatException) {
            MiraclError(
                value = Error(
                    CryptoResponses.ERROR_PIN.message
                ), exception = ex
            )
        } catch (ex: Exception) {
            MiraclError(
                value = Error(ex.message ?: CryptoResponses.ERROR_SIGNING.message),
                exception = ex
            )
        }
    }

    suspend fun acquirePin(pinProvider: PinProvider): String? {
        val semaphore = Semaphore(SEMAPHORE_PERMITS, SEMAPHORE_ACQUIRED_PERMITS)

        var pinEntered: String? = null

        pinProvider.provide { pin ->
            pinEntered = pin
            semaphore.release()
        }
        semaphore.acquire()

        return pinEntered
    }

    private fun isValidPinLength(pin: String, requiredPinLength: Int): Boolean {
        return pin.length == requiredPinLength
    }

    private fun logOperationStarted(operationName: String) {
        miraclLogger?.debug(CRYPTO_TAG, CRYPTO_OPERATION_STARTED.format(operationName))
    }

    private fun logOperationFinished(operationName: String) {
        miraclLogger?.debug(CRYPTO_TAG, CRYPTO_OPERATION_FINISHED.format(operationName))
    }
}
