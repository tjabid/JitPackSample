package tg.sdk.sca.services

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import tg.sdk.sca.BuildConfig
import tg.sdk.sca.R
import tg.sdk.sca.presentation.ui.enrollment.TgDashboardActivity
import tg.sdk.sca.presentation.ui.enrollment.TgDialogActivity
import tg.sdk.sca.presentation.utils.SdkConstants
import tg.sdk.sca.presentation.utils.SdkConstants.KEY_PUSH_TOKEN
import timber.log.Timber


private const val APPROVE_CONSENT_REQUEST_CHANNEL = "mpin_authentication_notification_channel"

open class TgSdkFirebaseMessagingService: FirebaseMessagingService() {

    private lateinit var sharedPref: SharedPreferences

    override fun onCreate() {
        super.onCreate()

        sharedPref =
            getSharedPreferences(SdkConstants.SCA_SDK_SHARED_PREFS_FILENAME, Context.MODE_PRIVATE)

        val channelName = "Channel ID"
        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && manager.getNotificationChannel(
                channelName
            ) == null) {
            val channel = NotificationChannel(
                channelName,
                "Default channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        Timber.d("TgSdkFirebaseMessaging Refreshed token: $token")

        saveNewToken(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        Timber.d("TgSdkFirebaseMessaging onMessageReceived: $remoteMessage\n data: ${remoteMessage.data.toList()}")

        val documentId = remoteMessage.data[KEY_DOCUMENT_ID]

        if (shouldStartConsentAsActivity()) {
            startActivity(
                TgDialogActivity.getAuthenticationLaunchIntent(
                    context = this,
                    sessionId = documentId
                )
            )
        } else {
            showConsentNotification(
                intent = TgDashboardActivity.getAuthenticationLaunchIntent(
                    context = this,
                    sessionId = documentId
                ),
                message = remoteMessage
            )
        }
    }

    private fun saveNewToken(newToken: String) {
        val stored = sharedPref.getString(KEY_PUSH_TOKEN, "")
        if (stored != newToken) {
            sharedPref.edit().putString(KEY_PUSH_TOKEN, newToken).apply()
        }
    }

    private fun showConsentNotification(
        intent: Intent,
        message: RemoteMessage
    ) {
        val pendingIntent: PendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val title = message.notification?.title
        val body = message.notification?.body
        val builder = NotificationCompat.Builder(this, APPROVE_CONSENT_REQUEST_CHANNEL)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(body)
            .setLights(Color.RED, 3000, 3000)
            .setVibrate(longArrayOf(200L, 200L))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                APPROVE_CONSENT_REQUEST_CHANNEL,
                "Default channel",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            manager.createNotificationChannel(channel)
        }
        manager.notify(System.currentTimeMillis().toInt(), builder.build())
    }

    private fun shouldStartConsentAsActivity() = BuildConfig.FLAVOR == SdkConstants.BuildFlavour.KFH

    companion object {

        const val KEY_DOCUMENT_ID = "documentID"

        fun handleNotificationIntent(intent: Intent?, context: Context) {
            if (intent != null && intent.hasExtra(KEY_DOCUMENT_ID)) {
                context.startActivity(
                    TgDashboardActivity.getAuthenticationLaunchIntent(
                        context = context,
                        sessionId = intent.getStringExtra(KEY_DOCUMENT_ID)
                    )
                )
            }
        }
    }
}