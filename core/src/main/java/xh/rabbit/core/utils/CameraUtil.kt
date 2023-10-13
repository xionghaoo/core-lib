package xh.rabbit.core.utils

import android.content.Context
import android.content.res.Configuration
import android.graphics.ImageFormat
import android.graphics.Rect
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.util.Log
import android.util.Size
import android.view.ViewGroup

class CameraUtil {
    companion object {
        fun createCameraPreview(
            context: Context,
            cameraIndex: Int,
            previewRect: Rect,
            cameraLayout: ViewGroup?,
            complete: (id: String, Size) -> Unit
        ) {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
            val index = if (cameraIndex >= cameraManager.cameraIdList.size) 0 else cameraIndex
            val cameraId: String = cameraManager.cameraIdList[index]
            val characteristic = try {
                cameraManager.getCameraCharacteristics(cameraId)
            } catch (e: Exception) {
                e.printStackTrace()
                return
            }
            // 打开第一个摄像头
            val configurationMap = characteristic.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)
            configurationMap?.getOutputSizes(ImageFormat.JPEG)
                ?.maxByOrNull {
                    Logger.d("camera size: ${it.width} x ${it.height}")
                    it.height * it.width
                }
                ?.also { maxImageSize ->
                    // Nexus6P相机支持的最大尺寸：4032x3024
//                    Timber.d(TAG, "相机支持的最大尺寸：${maxImageSize}")
                    // Nexus6P屏幕尺寸：1440 x 2560，包含NavigationBar的高度
//                    Log.d(BaseCameraActivity.TAG, "屏幕尺寸：${metrics.width()} x ${metrics.height()}")
//                    val layout =  cameraLayout
                    var previewAreaSize = Size(0, 0)
                    if (cameraLayout != null) {
                        val lp = cameraLayout.layoutParams as ViewGroup.LayoutParams

                        Logger.d("屏幕方向: ${if (context.resources.configuration.orientation == 1) "竖直" else "水平"}")
                        if (context.resources.configuration.orientation == Configuration.ORIENTATION_PORTRAIT) {
                            // 竖直方向：宽度优先，设置预览区域的尺寸，这个尺寸用于接收SurfaceTexture的显示
                            val ratio = maxImageSize.height.toFloat() / maxImageSize.width.toFloat()
                            lp.width = previewRect.width()
                            // Nexus6P 竖直方向屏幕计算高度
                            // 等比例关系：1440 / height = 3024 / 4032
                            // height = 4032 / 3024 * 1440
                            lp.height = (previewRect.width() / ratio).toInt()
                        } else {
                            // 水平方向：高度优先，设置预览区域的尺寸，这个尺寸用于接收SurfaceTexture的显示
                            val ratio = maxImageSize.height.toFloat() / maxImageSize.width.toFloat()
                            // Nexus6P 竖直方向屏幕计算高度
                            // 等比例关系：width / 1440 = 4032 / 3024
                            // width = 4032 / 3024 * 1440
                            lp.width = (previewRect.height() / ratio).toInt()
                            lp.height = previewRect.height()
                        }
                        previewAreaSize = Size(lp.width, lp.height)
                        Logger.d("预览区尺寸：${previewAreaSize}")
                    }
                    complete(cameraId, previewAreaSize)
//                    onCameraAreaCreated(
//                        cameraId,
//                        previewAreaSize,
//                        Size(metrics.width(), metrics.height()),
//                        Size(maxImageSize.width, maxImageSize.height)
//                    )
                }
        }
    }
}