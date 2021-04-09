package tg.sdk.sca.presentation.utils

import android.Manifest
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.LabeledIntent
import android.net.Uri
import android.os.StrictMode
import android.os.StrictMode.VmPolicy
import android.provider.MediaStore
import android.provider.Settings
import androidx.annotation.RequiresPermission
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import tg.sdk.sca.BuildConfig
import tg.sdk.sca.R
import tg.sdk.sca.data.biometric.BiometricPromptUtils
import java.io.File

@RequiresPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)

@RequiresPermission(Manifest.permission.CAMERA)
val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)

@RequiresPermission(Manifest.permission.CAMERA)
fun getCameraIntent(uri: Uri) = cameraIntent.apply {
    putExtra(MediaStore.EXTRA_OUTPUT, uri)
    flags = Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION
}

fun openAppSettings(context: Context) {
    context.startActivity(
        Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.parse("package:" + context.packageName)
        )
    )
}

fun Context.shareText(text: String) {
    startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_SEND).apply {
                type = "text/plain"
                putExtra(Intent.EXTRA_TEXT, text)
            },
            null
        )
    )
}

fun openEmailClient(context: Context) {
    context.startActivity(
        Intent.createChooser(
            Intent(Intent.ACTION_MAIN).apply {
                addCategory(Intent.CATEGORY_APP_EMAIL)
            },
            null
        )
    )
}

// todo verify
//fun openContactUs(context: Context) {
//    val intent = Intent(Intent.ACTION_VIEW).apply {
//        data = Uri.parse(
//            "mailto:${BuildConfig.SUPPORT_EMAIL}?subject=${context.getString(R.string.email_help)}"
//        )
//    }
//    context.startActivity(Intent.createChooser(intent, null))
//}

fun shareFile(
    context: Context,
    uri: Uri,
    fileType: String?
) {
    val pm = context.packageManager
    val intentShareFile = Intent(Intent.ACTION_SEND)
    val viewIntent = Intent(Intent.ACTION_VIEW)
    val file = File(uri.path ?: "")
    if (file.exists()) {
        val builder = VmPolicy.Builder()
        StrictMode.setVmPolicy(builder.build())
        intentShareFile.type = fileType
        intentShareFile.putExtra(Intent.EXTRA_STREAM, uri)
        viewIntent.type = fileType
        viewIntent.putExtra(Intent.EXTRA_STREAM, uri)
    }
    val openInChooser = Intent.createChooser(intentShareFile, "")
    openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, arrayOf(viewIntent))
    val resInfo = pm.queryIntentActivities(viewIntent, 0)
    val extraIntents = arrayOfNulls<Intent>(resInfo.size)
    for (i in resInfo.indices) { // Extract the label, append it, and repackage it in a LabeledIntent
        val ri = resInfo[i]
        val packageName = ri.activityInfo.packageName
        val intent = Intent()
        intent.component = ComponentName(packageName, ri.activityInfo.name)
        intent.action = Intent.ACTION_VIEW
        intent.setDataAndType(uri, fileType)
        val label = ri.loadLabel(pm)
        extraIntents[i] = LabeledIntent(intent, packageName, label, ri.icon)
    }
    openInChooser.putExtra(Intent.EXTRA_INITIAL_INTENTS, extraIntents)
    context.startActivity(openInChooser)
}

fun Fragment.goToSecuritySettings() = startActivityForResult(Intent(Settings.ACTION_SECURITY_SETTINGS),
    BiometricPromptUtils.REQUEST_CODE_BIOMETRIC_ENROLL
)
fun FragmentActivity.goToSecuritySettings() = startActivity(Intent(Settings.ACTION_SECURITY_SETTINGS))