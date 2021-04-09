package tg.sdk.sca.presentation.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build


object ConnectionUtil {

    @Suppress("DEPRECATION")
    fun isNetworkAvailable(context: Context) =
            (context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager).run {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    getNetworkCapabilities(activeNetwork)?.run {
                        hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                                || hasTransport(NetworkCapabilities.TRANSPORT_WIFI)
                                || hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
                    } ?: false

                } else {
                    val activeNetworkInfo = this.activeNetworkInfo
                    if (activeNetworkInfo != null) { // connected to the internet
                        // connected to the mobile provider's data plan
                        return if (activeNetworkInfo.type == ConnectivityManager.TYPE_WIFI) {
                            // connected to wifi
                            true
                        } else activeNetworkInfo.type == ConnectivityManager.TYPE_MOBILE
                    }

                    return false
                }
            }
}