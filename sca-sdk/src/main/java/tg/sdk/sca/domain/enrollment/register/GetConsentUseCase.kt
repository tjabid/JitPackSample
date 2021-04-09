package tg.sdk.sca.domain.enrollment.register

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.common.network.ResponseHandler
import tg.sdk.sca.data.common.network.Result
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.data.consent.RetrieveConsentRequest
import tg.sdk.sca.di.data.module.SdkRetrofitModule.scaServiceObj

class GetConsentUseCase {

    suspend fun execute(
        request: RetrieveConsentRequest,
        resultHandler: (Result<TgBobfConsent?>) -> Unit
    ) {

        scaServiceObj.getConsent(
            request = request
        ).enqueue(object : Callback<TgBobfConsent?> {
            override fun onResponse(
                call: Call<TgBobfConsent?>,
                response: Response<TgBobfConsent?>
            ) {
                resultHandler.invoke(ResponseHandler.handleResponse(response))
            }

            override fun onFailure(call: Call<TgBobfConsent?>, t: Throwable) {
                resultHandler.invoke(ResponseHandler.handleFailure(t))
            }
        })

    }
}