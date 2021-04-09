package tg.sdk.sca.presentation.utils.extensions.ui
//
//import android.annotation.SuppressLint
//import android.content.Context
//import android.graphics.Bitmap
//import android.graphics.drawable.Drawable
//import android.net.Uri
//import android.widget.ImageView
//import androidx.annotation.DrawableRes
//import androidx.annotation.Px
//import androidx.swiperefreshlayout.widget.CircularProgressDrawable
//import com.bumptech.glide.Glide
//import com.bumptech.glide.RequestBuilder
//import com.bumptech.glide.load.DataSource
//import com.bumptech.glide.load.Key
//import com.bumptech.glide.load.engine.DiskCacheStrategy
//import com.bumptech.glide.load.engine.GlideException
//import com.bumptech.glide.load.model.GlideUrl
//import com.bumptech.glide.load.resource.bitmap.RoundedCorners
//import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
//import com.bumptech.glide.request.RequestListener
//import com.bumptech.glide.request.RequestOptions
//import com.bumptech.glide.request.target.Target
//import com.bumptech.glide.request.transition.DrawableCrossFadeFactory
//import tg.sdk.sca.R
//import tg.sdk.sca.presentation.utils.extensions.UNDEFINED_INT
//import tg.sdk.sca.presentation.utils.toBitmap
//
///**
// * @param auth - determines whether must be attached Authorization header or not
// */
//@SuppressLint("CheckResult")
//fun ImageView.displayImage(
//    url: String?,
//    placeholder: Int = UNDEFINED_INT,
//    scaleType: ScaleType? = null,
//    @Px cornerRadius: Int? = null,
//    size: Size? = null,
//    auth: Boolean = false,
//    signature: Key? = null,
//    onLoaded: (() -> Unit)? = null
//) {
//    load(
//        context,
//        url,
//        this,
//        generateRequestOptions(scaleType, cornerRadius, size)
//            .apply {
//                if (placeholder != UNDEFINED_INT) {
//                    placeholder(placeholder)
//                    error(placeholder)
//                }
//            },
//        auth,
//        signature,
//        onLoaded
//    )
//}
//
//@SuppressLint("CheckResult")
//fun ImageView.displayImage(
//    file: Uri?,
//    @DrawableRes placeholder: Int = UNDEFINED_INT,
//    scaleType: ScaleType? = null,
//    cornerRadius: Int? = null,
//    size: Size? = null
//) {
//    load(
//        context,
//        file,
//        this,
//        generateRequestOptions(scaleType, cornerRadius, size)
//            .apply {
//                if (placeholder != UNDEFINED_INT) {
//                    placeholder(placeholder)
//                    error(placeholder)
//                }
//            }
//    )
//}
//
//@SuppressLint("CheckResult")
//fun ImageView.displayImage(
//    bitmap: Bitmap?,
//    @DrawableRes placeholder: Int = UNDEFINED_INT,
//    scaleType: ScaleType? = null,
//    cornerRadius: Int? = null,
//    size: Size? = null
//) {
//    load(
//        context,
//        bitmap,
//        this,
//        generateRequestOptions(scaleType, cornerRadius, size)
//            .apply {
//                if (placeholder != UNDEFINED_INT) {
//                    placeholder(placeholder)
//                    error(placeholder)
//                }
//            }
//    )
//}
//
//@SuppressLint("CheckResult")
//fun ImageView.displayImage(
//    url: String?,
//    placeholder: Drawable? = null,
//    errorPlaceholder: Int = UNDEFINED_INT,
//    scaleType: ScaleType? = null,
//    @Px cornerRadius: Int? = null,
//    size: Size? = null
//) {
//    load(
//        context,
//        url,
//        this,
//        generateRequestOptions(scaleType, cornerRadius, size)
//            .apply {
//                if (placeholder != null) {
//                    placeholder(placeholder)
//                }
//                if (errorPlaceholder != UNDEFINED_INT) {
//                    error(errorPlaceholder)
//                }
//            }
//    )
//}
//
//fun createProgressDrawable(context: Context) = CircularProgressDrawable(context).apply {
//    strokeWidth = context.resources.getDimension(R.dimen.progress_small_stroke_width)
//    centerRadius = context.resources.getDimension(R.dimen.progress_small_stroke_width)
//    start()
//}
//
//private fun load(context: Context, file: Uri?, imageView: ImageView, options: RequestOptions) =
//    load(context, file?.toBitmap(context), imageView, options)
//
//private fun load(context: Context, bitmap: Bitmap?, imageView: ImageView, options: RequestOptions) {
//    Glide.with(context)
//        .load(bitmap)
//        .apply(options)
//        .transition(crossFade)
//        .into(imageView)
//}
//
///**
// * @param auth - determines whether must be attached Authorization header or not
// */
//private fun load(
//    context: Context,
//    url: String?,
//    imageView: ImageView,
//    options: RequestOptions,
//    auth: Boolean = false,
//    signature: Key? = null,
//    onLoaded: (() -> Unit)? = null
//) {
//    val requestManager = Glide.with(context)
//    val requestBuilder: RequestBuilder<Drawable>
//    requestBuilder =
//        if (auth && !url.isNullOrBlank()) requestManager.load(GlideUrl(url))
//        else requestManager.load(url)
//    signature?.let {
//        requestBuilder.signature(it)
//    }
//    onLoaded?.let {
//        requestBuilder.listener(object : RequestListener<Drawable> {
//            override fun onLoadFailed(
//                e: GlideException?,
//                model: Any?,
//                target: Target<Drawable>?,
//                isFirstResource: Boolean
//            ): Boolean {
//                onLoaded()
//                return false
//            }
//
//            override fun onResourceReady(
//                resource: Drawable?,
//                model: Any?,
//                target: Target<Drawable>?,
//                dataSource: DataSource?,
//                isFirstResource: Boolean
//            ): Boolean {
//                onLoaded()
//                return false
//            }
//        })
//    }
//    requestBuilder
//        .apply(options)
//        .transition(crossFade)
//        .into(imageView)
//}
//
//private fun generateRequestOptions(
//    scaleType: ScaleType? = null,
//    cornerRadius: Int? = null,
//    size: Size? = null
//) = RequestOptions().apply {
//    diskCacheStrategy(DiskCacheStrategy.AUTOMATIC)
//    size?.let { override(it.width, it.height) }
//    cornerRadius?.let { transform(RoundedCorners(cornerRadius)) }
//    when (scaleType) {
//        ScaleType.CENTER_CROP -> centerCrop()
//        ScaleType.CENTER_INSIDE -> centerInside()
//        ScaleType.FIT_CENTER -> fitCenter()
//        ScaleType.CIRCLE_CROP -> circleCrop()
//    }
//}
//
//private val crossFade by lazy {
//    val factory = DrawableCrossFadeFactory.Builder()
//        .setCrossFadeEnabled(true)
//        .build()
//    DrawableTransitionOptions().crossFade(factory)
//}
//
//enum class ScaleType {
//    CIRCLE_CROP,
//    CENTER_CROP,
//    CENTER_INSIDE,
//    FIT_CENTER
//}
//
//data class Size(val width: Int, val height: Int)