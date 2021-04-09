package tg.sdk.sca.data.common.network

interface BaseService {

    fun clearFailedServiceUnavailableRequests()

    fun retryFailedServiceUnavailableRequests()
}
