package tg.sdk.sca.presentation.utils.extensions.ui

import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.AbsoluteSizeSpan
import android.view.inputmethod.EditorInfo
import android.widget.EditText

fun EditText.addAfterTextChangedListener(listener: (s: Editable?) -> Unit) {
    addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(e: Editable?) {
            listener(e)
        }

        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // autogenerated
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            // autogenerated
        }
    })
}

fun EditText.addImeActionDoneListener(action: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_DONE) {
            action()
        }
        false
    }
}

fun EditText.addImeActionNextListener(action: () -> Unit) {
    setOnEditorActionListener { _, actionId, _ ->
        if (actionId == EditorInfo.IME_ACTION_NEXT) {
            action()
        }
        false
    }
}

fun EditText.allowOnlyIBanCharacters() {
    filters = filters.plus(
        listOf(
            InputFilter { s, _, _, _, _, _->
                s.replace(Regex("[^A-Z0-9]"), "")
            },
            InputFilter.AllCaps()
        )
    )
}

fun EditText.applyDecimalStyle() {
    if (text.isNotEmpty()) {
        var length = text.length
        if (text.contains(".")) {
            val start = text.indexOf(".")
            text.setSpan(
                    AbsoluteSizeSpan(18, true),
                    start,
                    text.length,
                    Spanned.SPAN_INCLUSIVE_INCLUSIVE
            )
            length = start
        }
        text.setSpan(
                AbsoluteSizeSpan(48, true),
                0,
                length,
                Spanned.SPAN_INCLUSIVE_INCLUSIVE
        )
    }
}