package tg.sdk.sca.di.data.module

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import tg.sdk.sca.BuildConfig.BASE_URL
import tg.sdk.sca.data.common.network.interceptor.CurlLoggingInterceptor
import tg.sdk.sca.data.common.network.interceptor.ScaCommonHeadersInterceptor
import tg.sdk.sca.data.registration.remote.ScaService


object SdkRetrofitModule {

    private val client: OkHttpClient by lazy {
        OkHttpClient.Builder().apply {
            addInterceptor(ScaCommonHeadersInterceptor())
            addInterceptor(CurlLoggingInterceptor())
        }.build()
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .client(client)
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val scaServiceObj: ScaService by lazy {
        retrofit.create(ScaService::class.java)
    }

}
