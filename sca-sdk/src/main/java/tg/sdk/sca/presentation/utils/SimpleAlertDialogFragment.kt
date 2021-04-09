package tg.sdk.sca.presentation.utils

import android.content.DialogInterface
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import tg.sdk.sca.R
import tg.sdk.sca.presentation.utils.extensions.UNDEFINED_INT
import tg.sdk.sca.presentation.utils.extensions.isUndefined

class SimpleAlertDialogFragment : DialogFragment(), DialogInterface.OnClickListener {

    private val lazyListener by lazy { targetFragment as? AlertDialogClickListener }
    private var listener: AlertDialogClickListener? = null
        get() {
            return if (field == null) lazyListener
            else field
        }

    private var requestCode = UNDEFINED_INT

    private var title: String? = null

    @StringRes
    private var titleId = UNDEFINED_INT

    private var message: String? = null

    @StringRes
    private var messageId = UNDEFINED_INT

    private var positiveButtonText: String? = null

    @StringRes
    private var positiveButtonTextId = UNDEFINED_INT

    private var negativeButtonText: String? = null

    @StringRes
    private var negativeButtonTextId = UNDEFINED_INT

    private var neutralButtonText: String? = null

    @StringRes
    private var neutralButtonTextId = UNDEFINED_INT

    private var isDialogCancelable = true

    override fun onCreateDialog(savedInstanceState: Bundle?) =
        with(AlertDialog.Builder(requireContext(), R.style.DefaultAlertDialogStyle)) {
            if (!titleId.isUndefined()) setTitle(titleId)
            else setTitle(title)

            if (!messageId.isUndefined()) setMessage(messageId)
            else setMessage(message)

            if (!positiveButtonTextId.isUndefined()) {
                setPositiveButton(positiveButtonTextId, this@SimpleAlertDialogFragment)
            } else if (positiveButtonText != null) {
                setPositiveButton(positiveButtonText, this@SimpleAlertDialogFragment)
            }

            if (!negativeButtonTextId.isUndefined()) {
                setNegativeButton(negativeButtonTextId, this@SimpleAlertDialogFragment)
            } else if (negativeButtonText != null) {
                setNegativeButton(negativeButtonText, this@SimpleAlertDialogFragment)
            }

            if (!neutralButtonTextId.isUndefined()) {
                setNeutralButton(neutralButtonTextId, this@SimpleAlertDialogFragment)
            } else if (neutralButtonText != null) {
                setNeutralButton(neutralButtonTextId, this@SimpleAlertDialogFragment)
            }

            create()
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        isCancelable = isDialogCancelable
    }

    override fun onClick(dialog: DialogInterface?, which: Int) {
        when (which) {
            DialogInterface.BUTTON_POSITIVE -> listener?.onPositiveClick(requestCode)
            DialogInterface.BUTTON_NEGATIVE -> listener?.onNegativeClick(requestCode)
            DialogInterface.BUTTON_NEUTRAL -> listener?.onNeutralClick(requestCode)
        }
        dismiss()
    }

    companion object {
        val TAG = SimpleAlertDialogFragment::class.java.name
        fun newInstance(
            requestCode: Int = UNDEFINED_INT,
            title: String? = null,
            @StringRes titleId: Int = UNDEFINED_INT,
            message: String? = null,
            @StringRes messageId: Int = UNDEFINED_INT,
            positiveButtonText: String? = null,
            negativeButtonText: String? = null,
            @StringRes positiveButtonTextId: Int = UNDEFINED_INT,
            @StringRes negativeButtonTextId: Int = UNDEFINED_INT,
            neutralButtonText: String? = null,
            @StringRes neutralButtonTextId: Int = UNDEFINED_INT,
            listener: AlertDialogClickListener? = null,
            isCancelable: Boolean = true
        ) = SimpleAlertDialogFragment().apply {
            this.requestCode = requestCode
            this.title = title
            this.titleId = titleId
            this.message = message
            this.messageId = messageId
            this.positiveButtonText = positiveButtonText
            this.negativeButtonText = negativeButtonText
            this.positiveButtonTextId = positiveButtonTextId
            this.negativeButtonTextId = negativeButtonTextId
            this.neutralButtonText = neutralButtonText
            this.neutralButtonTextId = neutralButtonTextId
            this.listener = listener
            this.isDialogCancelable = isCancelable
        }
    }
}