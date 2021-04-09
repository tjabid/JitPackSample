package tg.sdk.sca.domain.enrollment.register

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.common.network.ResponseHandler
import tg.sdk.sca.data.common.network.Result
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.data.consent.UpdateConsentRequest
import tg.sdk.sca.di.data.module.SdkRetrofitModule.scaServiceObj
import timber.log.Timber

class UpdateConsentUseCase {

    suspend fun execute(
        request: UpdateConsentRequest,
        resultHandler: (Result<Unit?>) -> Unit
    ) {

        scaServiceObj.updateConsent(
            request = request
        ).enqueue(object : Callback<Unit?> {
            override fun onResponse(
                call: Call<Unit?>,
                response: Response<Unit?>
            ) {
                resultHandler.invoke(ResponseHandler.handleResponse(response))
            }

            override fun onFailure(call: Call<Unit?>, t: Throwable) {
                resultHandler.invoke(ResponseHandler.handleFailure(t))
            }
        })

    }
}