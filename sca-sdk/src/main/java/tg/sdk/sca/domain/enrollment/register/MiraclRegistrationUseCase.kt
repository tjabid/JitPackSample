package tg.sdk.sca.domain.enrollment.register

import com.miracl.trust.MiraclResult
import com.miracl.trust.MiraclTrust
import com.miracl.trust.delegate.PinProvider
import com.miracl.trust.model.AuthenticationUser
import com.miracl.trust.model.SigningUser
import com.miracl.trust.registration.ActivationToken

class MiraclRegistrationUseCase {

    suspend fun execute(
        userId: String,
        activationToken: ActivationToken,
        pinProvider: PinProvider,
        pushToken: String?,
        resultHandler: (MiraclResult<AuthenticationUser, Error>) -> Unit) {

        MiraclTrust.getInstance().register(
            userId = userId,
            activationToken = activationToken,
            pinProvider = pinProvider,
            pushNotificationsToken = pushToken,
            resultHandler = resultHandler
        )
    }

    suspend fun executeSignUser(
        authenticationUser: AuthenticationUser,
        accessId: String,
        authenticationPinProvider: PinProvider,
        signingPinProvider: PinProvider,
        resultHandler: (MiraclResult<SigningUser, Error>) -> Unit) {

        MiraclTrust.getInstance().signingRegister(
            authenticationUser = authenticationUser,
            accessId = accessId,//todo verify this in new SDK: not required for signing user
            authenticationPinProvider = authenticationPinProvider,
            signingPinProvider = signingPinProvider,
            resultHandler = resultHandler
        )
    }
}