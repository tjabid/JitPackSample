package tg.sdk.sca.data.common.network

import javax.inject.Inject

class FailureRequestsManagerImpl
@Inject constructor() : FailureRequestsManager {

    override val serviceUnavailableFailureRetrier: FailureRequestsRetrier
            by lazy { FailureRequestsRetrierImpl() }
}