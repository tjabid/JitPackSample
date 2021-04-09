package tg.sdk.sca.presentation.ui.sdkdashboard

import android.app.Activity.RESULT_OK
import android.content.Intent
import androidx.core.view.isVisible
import androidx.databinding.ViewDataBinding
import androidx.lifecycle.ViewModelProvider
import tg.sdk.sca.R
import tg.sdk.sca.databinding.FragmentSdkDashboardBinding
import tg.sdk.sca.presentation.core.ui.BaseFragment
import tg.sdk.sca.presentation.core.viewmodel.TgBobfSdkViewModelFactory
import tg.sdk.sca.presentation.ui.enrollment.TgDashboardActivity
import tg.sdk.sca.presentation.ui.sdkdashboard.SdkDashboardViewModel.SdkDashboardViewState.*

internal const val REQ_CODE_ENROLLMENT = 10101

class SdkDashboardFragment: BaseFragment() {

    override val layout: Int = R.layout.fragment_sdk_dashboard

    private lateinit var binding: FragmentSdkDashboardBinding
    override lateinit var viewModel: SdkDashboardViewModel

    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentSdkDashboardBinding
    }

    override fun setupViews() {

        viewModelFactory = TgBobfSdkViewModelFactory(SdkDashboardViewModel())
        viewModel = ViewModelProvider(this, viewModelFactory).get(SdkDashboardViewModel::class.java)

        binding.notEnabledLayout.enableObpBtn.setOnClickListener {
            viewModel.enableObp()
        }
        binding.manageConsent.setOnClickListener {
            startActivity(TgDashboardActivity.getManageConsentLaunchIntent(requireContext()))
        }


        viewModel.viewState.observe(this) {
            it?.let { handleViewState(it) }
        }
        viewModel.error.observe(this) {
            if (!it.isNullOrEmpty()) {
                showErrorMessage(it)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        adjustInitialView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQ_CODE_ENROLLMENT) {
            if (resultCode == RESULT_OK) {
                adjustInitialView()
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun handleViewState(state: SdkDashboardViewModel.SdkDashboardViewState) {
        showProgress(
            when (state) {
                INITIAL_VIEW -> true
                AUTHENTICATION -> true
                ENROLLING -> {
                    startEnrollment()
                    true
                }
                NOT_ENABLED -> {
                    setObNotEnabled()
                    false
                }
                ENABLED -> {
                    setObEnabled()
                    false
                }
                ERROR -> {
                    adjustInitialView()
                    false
                }
                DONE -> false
            }
        )
    }

    private fun showProgress(isShowing: Boolean = true) {
        binding.progressInclude.componentProgress.isVisible = isShowing
        if (isShowing) {
            binding.componentSdkDashboard.isVisible = false
            binding.notEnabledLayout.componentObpNotEnabled.isVisible = false
        }
    }

    private fun startEnrollment() {
        viewModel.userJwtToken?.let {
            startActivityForResult(
                TgDashboardActivity.getEnrollmentLaunchIntent(
                    context = requireContext(),
                    userToken = it
                ),
                REQ_CODE_ENROLLMENT
            )
        }
    }

    private fun setObEnabled() {
        binding.componentSdkDashboard.isVisible = true
        binding.notEnabledLayout.componentObpNotEnabled.isVisible = false
    }

    private fun setObNotEnabled() {
        binding.componentSdkDashboard.isVisible = false
        binding.notEnabledLayout.componentObpNotEnabled.isVisible = true
    }

    private fun adjustInitialView() {
        viewModel.adjustInitialView()
    }

}