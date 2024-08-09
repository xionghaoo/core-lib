package xh.rabbit.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.os.SystemClock
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.nio.FloatBuffer

class ImageUtil {
    companion object {

        fun encodeToBase64(bm: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun decodeFromBase64(img: String): Bitmap? {
            val imgData = Base64.decode(img, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imgData, 0, imgData.size)
        }

        fun getBitmapFromAsset(context: Context, assetPath: String): Bitmap? {
            var bitmap: Bitmap? = null
            var inputStream: InputStream? = null
            try {
                inputStream = context.assets.open(assetPath)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitmap
        }

        fun flip(img: Bitmap) : Bitmap {
            val m = Matrix()
            m.setScale(-1f, 1f)
            return Bitmap.createBitmap(img, 0, 0, img.width, img.height, m, true)
        }
    }

}