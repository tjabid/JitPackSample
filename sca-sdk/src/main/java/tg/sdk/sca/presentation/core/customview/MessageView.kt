package tg.sdk.sca.presentation.core.customview

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.AttributeSet
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import tg.sdk.sca.R

private const val LAYOUT_APPEARANCE_TIME = 1500L
private const val ANIMATION_DURATION = 500L

class MessageView
@JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
    ConstraintLayout(context, attrs, defStyleAttr) {

    init {
        View.inflate(context, R.layout.layout_message, this)
        setupView()
    }

    private fun setupView() {
        layoutParams =
            ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        alpha = 0f
        y = -this.height.toFloat()
    }

    fun show(message: CharSequence) {
        if (alpha == 0f) {
            findViewById<TextView>(R.id.message_tv).text = message
            animate()
                .translationY(0f)
                .alpha(1f)
                .duration = ANIMATION_DURATION

            Handler(Looper.getMainLooper()).postDelayed({
                animate()
                    .translationY(-this.height.toFloat())
                    .alpha(0f)
                    .duration = ANIMATION_DURATION
            }, LAYOUT_APPEARANCE_TIME)
        }
    }
}