package tg.sdk.sca.presentation.ui.authenticate

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tg.sdk.sca.data.consent.TgAccount

/**
 * Added 3rd parameter for configure to multiple selection of checkbox enable or not
 * by default is false.
 */
class ConsentAccountAdapter(
    private var itemList: List<TgAccount>,
    private val showSwitch: Boolean = true,
    private val checkChangeListener: OnItemCheckChangeListener? = null
) : RecyclerView.Adapter<ConsentViewHolder>() {


    private var multiSelection = true
    private var viewHolderAndSelectionMap : HashMap<ConsentViewHolder, Boolean>
            = HashMap()
    var selectedAccounts: MutableList<String> = ArrayList()
    var listener = object: OnItemClickListener{
        override fun onItemClick(item: TgAccount, isChecked: Boolean) {
            if (isChecked) {
                selectedAccounts.add(item.accountId)
            } else {
                selectedAccounts.remove(item.accountId)
            }
            checkChangeListener?.onChange()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsentViewHolder {
        return ConsentViewHolder.from(parent,
            showSwitch,
            viewHolderAndSelectionMap,
            listener
        )
    }

    override fun onBindViewHolder(holder: ConsentViewHolder, position: Int) {
        holder.bind(itemList[position], multiSelection)
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(bankList: List<TgAccount>) {
        itemList = bankList
        notifyDataSetChanged()
    }

    fun setMultiSelectionEnable( enable: Boolean){
        multiSelection = enable
        notifyDataSetChanged()
    }

    interface OnItemCheckChangeListener {
        fun onChange()
    }

    interface OnItemClickListener {
        fun onItemClick(item: TgAccount, isChecked: Boolean)
    }
}
