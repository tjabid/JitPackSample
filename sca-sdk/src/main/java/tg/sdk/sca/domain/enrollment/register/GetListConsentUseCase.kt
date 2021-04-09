package tg.sdk.sca.domain.enrollment.register

import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tg.sdk.sca.data.common.network.BaseRemoteDataSource
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.common.network.ResponseHandler
import tg.sdk.sca.data.common.network.Result
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.data.consent.ListConsentRequest
import tg.sdk.sca.di.data.module.SdkRetrofitModule.scaServiceObj

class GetListConsentUseCase {

    suspend fun execute(
        request: ListConsentRequest,
        resultHandler: (Result<List<TgBobfConsent>?>) -> Unit
    ) {

        scaServiceObj.getConsentList(
            request = request
        ).enqueue(object : Callback<List<TgBobfConsent>?> {
            override fun onResponse(
                call: Call<List<TgBobfConsent>?>,
                response: Response<List<TgBobfConsent>?>
            ) {
                resultHandler.invoke(ResponseHandler.handleResponse(response))
            }

            override fun onFailure(call: Call<List<TgBobfConsent>?>, t: Throwable) {
                resultHandler.invoke(ResponseHandler.handleFailure(t))
            }
        })

    }
}