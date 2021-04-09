package tg.sdk.sca.data.common.network

interface FailureRequestsManager {

    val serviceUnavailableFailureRetrier: FailureRequestsRetrier
}