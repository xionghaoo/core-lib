package xh.rabbit.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
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

const val DIM_BATCH_SIZE = 1;
const val DIM_PIXEL_SIZE = 3;
const val IMAGE_SIZE_X = 136;
const val IMAGE_SIZE_Y = 136;

class ImageUtil {
    companion object {

        fun encodeImage(bm: Bitmap): String? {
            val baos = ByteArrayOutputStream()
            bm.compress(Bitmap.CompressFormat.JPEG, 100, baos)
            val b = baos.toByteArray()
            return Base64.encodeToString(b, Base64.DEFAULT)
        }

        fun decodeImage(img: String): Bitmap? {
            val imgData = Base64.decode(img, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(imgData, 0, imgData.size)
        }

        fun getBitmapFromAsset(context: Context, path: String): Bitmap? {
            var bitmap: Bitmap? = null
            var inputStream: InputStream? = null
            try {
                inputStream = context.assets.open(path)
                bitmap = BitmapFactory.decodeStream(inputStream)
            } catch (e: IOException) {
                e.printStackTrace()
            }
            return bitmap
        }

        fun preProcess(bitmap: Bitmap): FloatBuffer {
            val imgData = FloatBuffer.allocate(
                        DIM_BATCH_SIZE
                        * DIM_PIXEL_SIZE
                        * IMAGE_SIZE_X
                        * IMAGE_SIZE_Y
            )
            imgData.rewind()
            val stride = IMAGE_SIZE_X * IMAGE_SIZE_Y
            val bmpData = IntArray(stride)
            bitmap.getPixels(bmpData, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
            for (i in 0..IMAGE_SIZE_X - 1) {
                for (j in 0..IMAGE_SIZE_Y - 1) {
                    val idx = IMAGE_SIZE_Y * i + j
                    val pixelValue = bmpData[idx]
                    imgData.put(idx, ((pixelValue shr 16 and 0xFF) / 127.5f - 1.0f))
                    imgData.put(idx + stride, ((pixelValue shr 8 and 0xFF) / 127.5f - 1.0f))
                    imgData.put(idx + stride * 2, ((pixelValue and 0xFF) / 127.5f - 1.0f))
                }
            }

            imgData.rewind()
            return imgData
        }

        fun preProcessBatch(bitmaps: List<Bitmap>,dim_batch_size:Int): FloatBuffer {
            var start = SystemClock.uptimeMillis()
            val imgData = FloatBuffer.allocate(
                dim_batch_size
                        * DIM_PIXEL_SIZE
                        * IMAGE_SIZE_X
                        * IMAGE_SIZE_Y
            )
            imgData.rewind()
            val stride = IMAGE_SIZE_X * IMAGE_SIZE_Y
            val bmpData = IntArray(stride)
            var num = 0 // 偏移量
            bitmaps.forEach{bitmap ->
                bitmap.getPixels(bmpData, 0, bitmap.width, 0, 0, bitmap.width, bitmap.height)
                val offset = num*stride*3
                for (i in 0..IMAGE_SIZE_X - 1) {
                    for (j in 0..IMAGE_SIZE_Y - 1) {
                        val idx = IMAGE_SIZE_Y * i + j
                        val pixelValue = bmpData[idx]
                        imgData.put(idx+offset, ((pixelValue shr 16 and 0xFF) / 127.5f - 1.0f))
                        imgData.put(idx + stride+offset, ((pixelValue shr 8 and 0xFF) / 127.5f - 1.0f))
                        imgData.put(idx + stride*2+ offset, ((pixelValue and 0xFF) / 127.5f - 1.0f))
                    }
                }
                num++
            }
            imgData.rewind()
            Log.d("ImageUtil", "preProcessBatch cost time = ${SystemClock.uptimeMillis() - start}")
            return imgData
        }
    }
}