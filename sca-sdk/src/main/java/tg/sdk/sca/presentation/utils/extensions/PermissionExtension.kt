package tg.sdk.sca.presentation.utils.extensions

import android.Manifest
import android.content.pm.PackageManager
import androidx.annotation.StringDef
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

fun Fragment.checkSelfPermission(
    @Permission permission: String,
    reqCode: Int,
    onGranted: () -> Unit,
    onDenied: () -> Unit
) {
    when {
        ContextCompat.checkSelfPermission(
            requireContext(),
            permission
        ) == PackageManager.PERMISSION_GRANTED -> onGranted()
        ActivityCompat.shouldShowRequestPermissionRationale(
            requireActivity(),
            permission
        ) -> onDenied()
        else -> requestPermissions(arrayOf(permission), reqCode)
    }
}

@StringDef(Permission.CAMERA, Permission.READ_EXTERNAL_STORAGE)
annotation class Permission {
    companion object {
        const val CAMERA = Manifest.permission.CAMERA
        const val READ_EXTERNAL_STORAGE = Manifest.permission.READ_EXTERNAL_STORAGE
    }
}