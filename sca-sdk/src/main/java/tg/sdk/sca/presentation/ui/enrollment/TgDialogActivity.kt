package tg.sdk.sca.presentation.ui.enrollment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import tg.sdk.sca.databinding.ActivityDialogBinding
import tg.sdk.sca.presentation.core.ui.BaseActivity

class TgDialogActivity: BaseActivity() {

    private lateinit var binding: ActivityDialogBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        binding = ActivityDialogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setFinishOnTouchOutside(false);

        val sessionId = intent?.extras?.getString(SESSION_ID_ARG)
        if (sessionId.isNullOrEmpty()) {
            finish()
        }

        binding.alertVerify.setOnClickListener {
            startActivity(
                TgDashboardActivity.getAuthenticationLaunchIntent(
                    context = this,
                    sessionId = sessionId
                )
            )
            finish()
        }

        binding.alertCancel.setOnClickListener {
            finish()
        }
    }

    companion object {

        private const val SESSION_ID_ARG = "sessionId"

        internal fun getAuthenticationLaunchIntent(
            context: Context,
            sessionId: String?
        ) = Intent(context, TgDialogActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS
            putExtra(SESSION_ID_ARG, sessionId)
        }
    }
}