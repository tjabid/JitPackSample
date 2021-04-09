package tg.sdk.sca.data.common.network

import retrofit2.Call
import retrofit2.Callback

internal class FailureRequestsRetrierImpl : FailureRequestsRetrier {

    private val requests: MutableSet<RequestRetrier<*>> by lazy { mutableSetOf<RequestRetrier<*>>() }

    @Synchronized
    override fun <T> put(call: Call<T>, callback: Callback<T>) {
        val request =
            RequestRetrier(call, callback)
        requests.add(request)
    }

    @Synchronized
    override fun retry() {
        val iterator = requests.iterator()
        while (iterator.hasNext()) {
            val retrier: RequestRetrier<*> = iterator.next()
            retrier.retry()
            iterator.remove()
        }
    }

    @Synchronized
    override fun clear() {
        requests.clear()
    }

    private class RequestRetrier<T>(private val call: Call<T>, private val callback: Callback<T>) {

        fun retry() {
            call.enqueue(callback)
        }
    }
}