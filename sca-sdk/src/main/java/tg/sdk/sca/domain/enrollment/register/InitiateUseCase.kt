package tg.sdk.sca.domain.enrollment.register

import android.os.Build
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import tg.sdk.sca.tgbobf.TgBobfSdk
import tg.sdk.sca.data.common.network.NetworkError
import tg.sdk.sca.data.common.network.ResponseHandler
import tg.sdk.sca.data.common.network.Result
import tg.sdk.sca.data.registration.model.ActivationToken
import tg.sdk.sca.di.data.module.SdkRetrofitModule.scaServiceObj

class InitiateUseCase {

    suspend fun execute(resultHandler: (Result<ActivationToken?>) -> Unit) {

        val hashMap = HashMap<String, String>()
        hashMap["deviceName"] = Build.MODEL
        hashMap["userToken"] = TgBobfSdk.token ?: "userToken"

        scaServiceObj.fetchActivationToken(
            hashMap
        ).enqueue(object : Callback<ActivationToken?> {
            override fun onResponse(
                call: Call<ActivationToken?>,
                response: Response<ActivationToken?>
            ) {
                resultHandler.invoke(ResponseHandler.handleResponse(response))
            }

            override fun onFailure(call: Call<ActivationToken?>, t: Throwable) {
                resultHandler.invoke(ResponseHandler.handleFailure(t))
            }
        })
    }
}