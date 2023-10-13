package xh.rabbit.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.ParcelFileDescriptor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import xh.rabbit.core.showToast
import java.io.File
import java.io.FileOutputStream

class PdfUtil {
    companion object {
        fun convertPdfToImages(
            pdf: String,
            outDir: File,
            success: (List<File>) -> Unit,
            failure: (e: String?) -> Unit
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                kotlin.runCatching {
                    val fileDescriptor = ParcelFileDescriptor.open(
                        File(pdf),
                        ParcelFileDescriptor.MODE_READ_ONLY
                    )
                    val renderer = PdfRenderer(fileDescriptor)
                    val pageCount = renderer.pageCount
                    Logger.d("parse pdf pages: ${pageCount}")
                    val images = ArrayList<File>()
                    for (i in 0 until pageCount) {
                        val page = renderer.openPage(i)
                        val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
                        val canvas = Canvas(bitmap!!)
                        canvas.drawColor(Color.WHITE)
                        canvas.drawBitmap(bitmap, 0f, 0f, null)
                        page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                        page.close()
                        if (!bitmapIsBlankOrWhite(bitmap)) {
                            val pdfName = pdf.split("/").last().removeSuffix(".pdf")
                            val name = "${pdfName}_page_${i}.png"
                            val file = File(outDir, name)
                            images.add(file)
                            if (file.exists()) file.delete()
                            var out: FileOutputStream? = null
                            try {
                                out = FileOutputStream(file)
                                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                                Logger.v("save pdf page: ${file.absolutePath}")
                                out.flush()
                            } catch (e: Exception) {
                                e.printStackTrace()
                            } finally {
                                out?.close()
                                out = null
                            }
                        }
                    }
                    withContext(Dispatchers.Main) {
                        success(images)
                    }
                }.onFailure { e ->
                    Logger.e(e)
                    withContext(Dispatchers.Main) {
                        failure(e.localizedMessage)
                    }
                }
            }
        }

        private fun bitmapIsBlankOrWhite(bitmap: Bitmap?): Boolean {
            if (bitmap == null) return true
            val w = bitmap.width
            val h = bitmap.height
            for (i in 0 until w) {
                for (j in 0 until h) {
                    val pixel = bitmap.getPixel(i, j)
                    if (pixel != Color.WHITE) {
                        return false
                    }
                }
            }
            return true
        }
    }
}