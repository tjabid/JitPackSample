package tg.sdk.sca.presentation.ui.authenticate

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import tg.sdk.sca.data.consent.TgAccount
import tg.sdk.sca.databinding.CellAccountsBinding
import java.lang.ref.WeakReference

/**
 * ViewHolder contains
 * multiSelectionEnable boolean which
 * controlled the multiple selection or single selection in a data group
 *
 */
class ConsentViewHolder(
    private val binding: CellAccountsBinding,
    private val showSwitch: Boolean,
    private var viewHolderAndSelectionMap: HashMap<ConsentViewHolder, Boolean>,
    private val listenerRef: WeakReference<ConsentAccountAdapter.OnItemClickListener?>
) : RecyclerView.ViewHolder(binding.root){

    fun bind(item: TgAccount, multiSelectionEnable: Boolean) {
        binding.checkboxAccount.isVisible = showSwitch
        if (showSwitch) {
            if(!multiSelectionEnable) {
                //if single selection checkbox enable
                viewHolderAndSelectionMap[this] = false
                binding.checkboxAccount.setOnClickListener {
                    invalidateCheckBox(this@ConsentViewHolder)
                }
            }
            binding.checkboxAccount.setOnCheckedChangeListener { _, isChecked ->
                listenerRef.get()?.onItemClick(item, isChecked)
            }
        }
        binding.account = item
        binding.executePendingBindings()
    }

    private fun invalidateCheckBox(viewHolder: ConsentViewHolder) {
        for(key in viewHolderAndSelectionMap.keys){
            if(key != viewHolder) {
                key.binding.checkboxAccount.isChecked = false
            }
        }
    }


    companion object {
        fun from(parent: ViewGroup,
                 showSwitch: Boolean,
                 viewHolderAndSelectionMap : HashMap<ConsentViewHolder, Boolean>,
                 listener: ConsentAccountAdapter.OnItemClickListener?
        ): ConsentViewHolder {
            val layoutInflater = LayoutInflater.from(parent.context)
            val binding = CellAccountsBinding.inflate(layoutInflater, parent, false)
            return ConsentViewHolder(binding, showSwitch, viewHolderAndSelectionMap, WeakReference(listener))
        }
    }
}