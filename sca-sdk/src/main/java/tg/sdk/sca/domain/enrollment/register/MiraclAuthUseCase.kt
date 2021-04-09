package tg.sdk.sca.domain.enrollment.register

import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclTrust
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.SigningUser
import com.miracl.trust.signing.Signature
import java.util.*

class MiraclAuthUseCase {

    suspend fun execute(
        message: ByteArray,
        signingUser: SigningUser,
        date: Date,
        pinProvider: PinProvider,
        resultHandler: (MiraclResult<Signature, Error>) -> Unit) {

        MiraclTrust.getInstance().sign(
            message = message,
            timestamp = date,
            signingUser = signingUser,
            pinProvider = pinProvider,
            resultHandler = resultHandler
        )
    }
}