package tg.sdk.sca.presentation.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.MediaStore
import androidx.core.content.FileProvider
//import androidx.exifinterface.media.ExifInterface
//import com.bumptech.glide.load.resource.bitmap.TransformationUtils.rotateImage
import java.io.File
import java.io.FileDescriptor
import java.io.FileNotFoundException
import java.io.IOException

private const val AUTHORITY = ".provider"

//fun File.toUri(context: Context): Uri =
//    FileProvider.getUriForFile(context, "${context.packageName}.provider", this)
//
//fun Uri.toFile(context: Context): File? = runCatching {
//    context.contentResolver.query(
//        this,
//        arrayOf(MediaStore.Images.ImageColumns.DATA),
//        null,
//        null,
//        null
//    )?.run {
//        if (!moveToFirst()) throw FileNotFoundException()
//        val path = getString(getColumnIndexOrThrow(MediaStore.Images.ImageColumns.DATA))
//        close()
//        if (!path.isNullOrBlank()) File(path)
//        else throw FileNotFoundException()
//    }
//}.onFailure {
//    it.printStackTrace()
//}.getOrNull()
//
//fun getUriForFile(context: Context, file: File): Uri? {
//    return FileProvider.getUriForFile(
//        context,
//        context.packageName + AUTHORITY, file
//    )
//}
//
//@Throws(IOException::class)
//fun Uri.toBitmap(context: Context): Bitmap {
//    context.contentResolver.openFileDescriptor(this, "r")?.let { descriptor ->
//        val fileDescriptor: FileDescriptor = descriptor.fileDescriptor
//        val image: Bitmap = BitmapFactory.decodeFileDescriptor(fileDescriptor)
//        descriptor.close()
//        return changeRotationIfNeed(this, image, context)
//    } ?: throw IOException()
//}
//
//private fun changeRotationIfNeed(
//    uri: Uri,
//    bitmap: Bitmap,
//    context: Context
//): Bitmap {
//    val inputStream = context.contentResolver.openInputStream(uri)
//    inputStream?.also {
//        val ei = ExifInterface(it)
//        val orientation: Int = ei.getAttributeInt(
//            ExifInterface.TAG_ORIENTATION,
//            ExifInterface.ORIENTATION_UNDEFINED
//        )
//        val rotatedBitmap = when (orientation) {
//            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90)
//            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180)
//            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270)
//            ExifInterface.ORIENTATION_NORMAL -> bitmap
//            else -> bitmap
//        }
//        it.close()
//        return rotatedBitmap
//    }
//    return bitmap
//}