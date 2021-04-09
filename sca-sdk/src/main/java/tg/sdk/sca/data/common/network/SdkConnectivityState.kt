package tg.sdk.sca.data.common.network

import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SdkConnectivityState @Inject constructor(private val connectivityManager: ConnectivityManager) :
    ConnectivityState {

    override fun hasConnection(): Boolean {
        val network = connectivityManager.activeNetwork
        val connection =
            connectivityManager.getNetworkCapabilities(network)
        return connection != null && (
                connection.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                        connection.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) ||
                        connection.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                )
    }
}