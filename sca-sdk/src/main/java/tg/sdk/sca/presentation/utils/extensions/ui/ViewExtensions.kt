package tg.sdk.sca.presentation.utils.extensions.ui

import android.view.View
import androidx.core.view.isVisible

fun View.toGone() {
    isVisible = false
}

fun View.toVisible(isVisible: Boolean = true) {
    visibility = if (isVisible) View.VISIBLE else View.INVISIBLE
}

fun View.setOnClickListenerWithDebounce(action: () -> Unit) {
    this.setOnClickListener(object : View.OnClickListener {
        private var lastClickTime: Long = 0

        override fun onClick(v: View) {
            if (System.currentTimeMillis() - lastClickTime < 1000L) return
            else action()
            lastClickTime = System.currentTimeMillis()
        }
    })
}