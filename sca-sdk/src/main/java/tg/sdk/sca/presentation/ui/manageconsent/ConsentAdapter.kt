package tg.sdk.sca.presentation.ui.manageconsent

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import tg.sdk.sca.data.consent.TgBobfConsent
import tg.sdk.sca.databinding.CellConsentBinding
import java.lang.ref.WeakReference

class ConsentAdapter(
    private var itemList: List<TgBobfConsent>,
    private var listener: OnItemClickListener?
) : RecyclerView.Adapter<ConsentAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(item: TgBobfConsent)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder.from(parent, listener)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position], position)
    }

    override fun getItemCount(): Int = itemList.size

    fun updateList(bankList: List<TgBobfConsent>) {
        itemList = bankList
        notifyDataSetChanged()
    }

    class ViewHolder private constructor(
            private val binding: CellConsentBinding,
            private val listenerRef: WeakReference<OnItemClickListener>?
    ) : RecyclerView.ViewHolder(binding.root){

        fun bind(item: TgBobfConsent, position: Int) {
            binding.cellConsent.setOnClickListener {
                listenerRef?.get()?.onItemClick(item)
            }
            binding.consent = item
            binding.executePendingBindings()
        }

        companion object {
            fun from(parent: ViewGroup, listener: OnItemClickListener?): ViewHolder {
                val layoutInflater = LayoutInflater.from(parent.context)
                val binding = CellConsentBinding.inflate(layoutInflater, parent, false)
                return ViewHolder(binding, WeakReference(listener))
            }
        }
    }
}