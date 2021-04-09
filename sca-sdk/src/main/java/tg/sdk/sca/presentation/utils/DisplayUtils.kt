package tg.sdk.sca.presentation.utils

import android.content.Context
import android.graphics.Point
import android.util.DisplayMetrics
import android.view.WindowManager
import androidx.annotation.Px

//@Px
//fun Context.getScreenWidth(): Int {
//    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
//    val size = Point()
//    display.getSize(size)
//    return size.x
//}
//
//@Px
//fun Context.getScreenHeight(): Int {
//    val display = (getSystemService(Context.WINDOW_SERVICE) as WindowManager).defaultDisplay
//    val size = Point()
//    display.getSize(size)
//    return size.y
//}

@Px
fun dpToPx(context: Context, dp: Float) =
    (dp * (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)).toInt()

fun pxToDp(context: Context, @Px px: Int): Float =
    px / (context.resources.displayMetrics.densityDpi.toFloat() / DisplayMetrics.DENSITY_DEFAULT)