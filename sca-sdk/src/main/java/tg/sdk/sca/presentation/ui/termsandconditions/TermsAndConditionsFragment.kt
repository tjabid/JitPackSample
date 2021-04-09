package tg.sdk.sca.presentation.ui.termsandconditions

import androidx.databinding.ViewDataBinding
import tg.sdk.sca.R
import tg.sdk.sca.databinding.FragmentTermsAndConditionsBinding
import tg.sdk.sca.presentation.core.ui.BaseFragment

class TermsAndConditionsFragment: BaseFragment() {

    override val layout: Int = R.layout.fragment_terms_and_conditions

    private lateinit var binding: FragmentTermsAndConditionsBinding


    override fun castBinding(viewBinding: ViewDataBinding) {
        binding = viewBinding as FragmentTermsAndConditionsBinding
    }

    override fun setupViews() {

        setupToolbar(binding.toolbarTermsAndConditions, getString(R.string.terms_conditions))

        binding.btnAccept.setOnClickListener {
            navController.navigate(TermsAndConditionsFragmentDirections.actionTermsToRegister())
        }
    }

}