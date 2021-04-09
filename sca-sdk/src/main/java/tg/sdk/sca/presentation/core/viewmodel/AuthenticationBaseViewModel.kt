package tg.sdk.sca.presentation.core.viewmodel

import androidx.lifecycle.viewModelScope
import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclTrust
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.SigningUser
import com.miracl.trust.signing.Signature
import com.miracl.trust.util.secondsSince1970
import com.miracl.trust.util.toHexString
import kotlinx.coroutines.launch
import tg.sdk.sca.domain.enrollment.register.MiraclAuthUseCase
import tg.sdk.sca.tgbobf.TgBobfSdk
import timber.log.Timber
import java.security.MessageDigest
import java.util.*

abstract class AuthenticationBaseViewModel : BaseViewModel() {

    var hasShownBiometricOption: Boolean = false

    val signingUser: SigningUser? by lazy {
        MiraclTrust.getInstance().signingUsers
            .find { user -> user.identity.userId == TgBobfSdk.userId } as SigningUser
    }

    fun getSignature(
        message: ByteArray,
        signingUser: SigningUser,
        date: Date,
        pinProvider: PinProvider,
        resultHandler: (MiraclResult<Signature, Error>) -> Unit
    ) {
        viewModelScope.launch(IO) {
            MiraclAuthUseCase().execute(
                message = message,
                signingUser = signingUser,
                date = date,
                pinProvider = pinProvider,
                resultHandler = resultHandler
            )
        }
    }

    fun getTimeStamp(date: Date): Int = date.secondsSince1970()

    fun getHash(msg: String): ByteArray? {
        Timber.d("HASHING $msg")
        try {
            return MessageDigest.getInstance("SHA-256").digest(msg.toByteArray())
        } catch (e: Exception) {
            Timber.e(e)
        }
        return null
    }

    fun getMPin() =
        MessageDigest.getInstance("SHA-256").digest(signingUser!!.identity.mpinId).toHexString()

}