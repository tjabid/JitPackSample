package tg.sdk.sca.tgbobf

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.fragment.app.Fragment
import com.miracl.trust.MiraclTrust
import com.miracl.trust.configuration.Configuration
import com.miracl.trust.util.log.MiraclLogger
import tg.sdk.sca.BuildConfig.*
import tg.sdk.sca.presentation.ui.sdkdashboard.SdkDashboardFragment
import tg.sdk.sca.presentation.utils.ConnectionUtil
import tg.sdk.sca.presentation.utils.SdkConstants.KEY_PREF_USER_ID
import tg.sdk.sca.presentation.utils.SdkConstants.SCA_SDK_SHARED_PREFS_FILENAME
import timber.log.Timber
import timber.log.Timber.DebugTree


object TgBobfSdk {

    var token: String? = null
    internal var userId: String? = null
    set(value) {
        field = value
        sharedPref.edit().putString(KEY_PREF_USER_ID, value).apply()
    }

    private lateinit var sharedPref: SharedPreferences
    internal var authorizer: (() -> Unit)? = null

    @JvmStatic
    fun init(
        application: Application
    ) {
        if (BUILD_TYPE != "release") {
            Timber.plant(DebugTree())
        }

        fetchSharedPrefValues(application)

        checkClientProperties()
        if (!ConnectionUtil.isNetworkAvailable(application.applicationContext)) {
            //todo - enhancement - implement retry functionality on internet re-connectivity
            Log.e("TgBobfSdk", "TG BOBF SDK can't start, please make sure your internet is working")
            return
        }

        configurationMiracl(application)
    }

    internal fun configurationMiracl(context: Context) {

        val configuration =
            Configuration.Builder(projectId = MIRACL_PROJECT_ID, clientId = MIRACL_CLIENT_ID)
                .logLevel(
                    if (DEBUG) {
                        MiraclLogger.LogLevel.DEBUG
                    } else {
                        MiraclLogger.LogLevel.NONE
                    }
                )
                .build()


        MiraclTrust.configure(context.applicationContext, configuration)
    }

    private fun fetchSharedPrefValues(application: Application) {
        sharedPref =
            application.getSharedPreferences(SCA_SDK_SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)

        userId = sharedPref.getString(KEY_PREF_USER_ID, "")
    }

    fun isUserEnrolled(): Boolean = !userId.isNullOrEmpty()

    private fun checkClientProperties() {
        if (MIRACL_PROJECT_ID.isEmpty()
            || MIRACL_CLIENT_ID.isEmpty()
        ) {
            throw RuntimeException("TG BOBF SDK can't start, please verify configuration with TG team")
        }
    }

    @JvmStatic
    fun onFetchUserTokenSuccess(userToken: String) =
        TgBobfUserTokenManager.onUserTokenSuccessful(userToken)

    @JvmStatic
    fun onFetchUserTokenError(message: String) = TgBobfUserTokenManager.onUserTokenError(message)

    @JvmStatic
    fun getSdkDashboardFragment(authorizer: (() -> Unit)): Fragment {
        this.authorizer = authorizer
        return SdkDashboardFragment()
    }

}