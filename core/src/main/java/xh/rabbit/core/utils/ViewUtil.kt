package xh.rabbit.core.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.SurfaceTexture
import android.os.Build
import android.os.Handler
import android.os.Looper
import android.util.TypedValue
import android.view.PixelCopy
import android.view.Surface
import android.view.View

class ViewUtil {
    companion object {
        /**
         * 对视图设置波纹效果
         *
         * @param context
         * @param view
         */
        fun setRippleBackground(context: Context?, view: View) {
            if (context == null) return
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            view.setBackgroundResource(outValue.resourceId)
        }

        /**
         * 带Surface的视图生成Bitmap
         *
         * @param view
         * @param success
         * @receiver
         */
        fun captureViewWithSurface(view: View, success: (Bitmap) -> Unit) {
            val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val surfaceTexture = SurfaceTexture(false)
                surfaceTexture.setDefaultBufferSize(view.width, view.height)
                val surface: Surface = Surface(surfaceTexture)
                val hwCanvas: Canvas = surface.lockHardwareCanvas()
                view.draw(hwCanvas)
                surface.unlockCanvasAndPost(hwCanvas)

                PixelCopy.request(surface, bitmap,
                    PixelCopy.OnPixelCopyFinishedListener { copyResult ->
                        if (copyResult != PixelCopy.SUCCESS) {
                            // failed to pixelcopy, fallback to software draw
                            view.draw(Canvas(bitmap))
                        }
                        surface.release()
                        surfaceTexture.release()
                        success(bitmap)
                    }, Handler(Looper.getMainLooper())
                )
            } else {
                view.draw(Canvas(bitmap))
                success(bitmap)
            }
        }
    }
}