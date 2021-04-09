package tg.sdk.sca.presentation.ui.enrollment

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.navigation.fragment.NavHostFragment
import tg.sdk.sca.R
import tg.sdk.sca.tgbobf.TgBobfSdk
import tg.sdk.sca.databinding.ActivityDashboardBinding
import tg.sdk.sca.presentation.core.ui.BaseActivity
import timber.log.Timber

class TgDashboardActivity: BaseActivity() {

    private lateinit var binding: ActivityDashboardBinding

    private val navHostFragmentId: Int = R.id.nav_host_fragment
    val navController: NavController by lazy {
        Navigation.findNavController(this, navHostFragmentId)
    }

    private val navGraphId: Int
        get() = R.navigation.dashboard_navigation

    override fun onCreate(savedInstanceState: Bundle?) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        super.onCreate(savedInstanceState)

        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        var bundle = bundleOf()

        when {
            intent?.hasExtra(SESSION_ID_ARG) == true -> {
                bundle = bundleOf(
                    SESSION_ID_ARG to intent?.getStringExtra(SESSION_ID_ARG)
                )
            }
            intent?.hasExtra(START_ENROLLMENT_ARG) == true -> {
                bundle = bundleOf(
                    START_ENROLLMENT_ARG to (intent?.getBooleanExtra(START_ENROLLMENT_ARG, false) ?: false)
                )
            }
            intent?.hasExtra(START_MANAGE_CONSENT_ARG) == true -> {
                bundle = bundleOf(
                    START_MANAGE_CONSENT_ARG to (intent?.getBooleanExtra(START_MANAGE_CONSENT_ARG, false) ?: false)
                )
            }
            else -> {
                //todo
            }
        }

        (supportFragmentManager.findFragmentById(navHostFragmentId) as? NavHostFragment)?.apply {
            val navGraph = navController.navInflater.inflate(navGraphId).apply {
                startDestination =
                    when {
                        bundle.containsKey(SESSION_ID_ARG) -> {
                            R.id.authenticationFragment
                        }
                        bundle.containsKey(START_ENROLLMENT_ARG) -> {
                            R.id.termsConditionsFragment
                        }
                        bundle.containsKey(START_MANAGE_CONSENT_ARG) -> {
                            R.id.manageConsentFragment
                        }
                        else -> {
                            R.id.dashboardFragment
                        }
                    }
            }
            navController.setGraph(navGraph, bundle)
        }

        Timber.d("TgDashboardActivity onCreate ${intent?.extras}")
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        Timber.d("TgDashboardActivity onNewIntent ${intent?.extras}")
    }

    companion object {

        internal const val START_ENROLLMENT_ARG = "START_ENROLLMENT_ARG"
        internal const val START_MANAGE_CONSENT_ARG = "START_MANAGE_CONSENT_ARG"
        internal const val SESSION_ID_ARG = "sessionId"

        fun getEnrollmentLaunchIntent(
            context: Context,
            userToken: String
        ): Intent {
            TgBobfSdk.token = userToken
            return Intent(context, TgDashboardActivity::class.java).apply {
                putExtra(START_ENROLLMENT_ARG, true)
            }
        }

        fun getDashboardLaunchIntent(
            context: Context
        ) = Intent(context, TgDashboardActivity::class.java)

        internal fun getAuthenticationLaunchIntent(
            context: Context,
            sessionId: String?
        ) = Intent(context, TgDashboardActivity::class.java).apply {
            putExtra(SESSION_ID_ARG, sessionId)
        }

        internal fun getManageConsentLaunchIntent(
            context: Context
        ) = Intent(context, TgDashboardActivity::class.java).apply {
            putExtra(START_MANAGE_CONSENT_ARG, true)
        }
    }
}