package xh.rabbit.core.utils

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.core.content.FileProvider
import java.io.File

class ApkUtil {
    companion object {
        /**
         * Install apk
         *
         * @param context
         * @param authority file provider, eg. ${BuildConfig.APPLICATION_ID}.provider
         * @param apk
         */
        fun installApk(context: Context, authority: String, apk: File) {
            val intent = Intent(Intent.ACTION_VIEW)
            //如果没有设置SDCard写权限，或者没有sdcard,apk文件保存在内存中，需要授予权限才能安装
            try {
                val command = arrayOf("chmod", "777", apk.toString())
                val builder = ProcessBuilder(*command)
                builder.start()

                var uri: Uri? = null
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                    uri = FileProvider.getUriForFile(context, authority, apk)
                    intent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                    intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                } else {
                    uri = Uri.fromFile(apk)
                }

                intent.setDataAndType(uri, "application/vnd.android.package-archive")
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
            } catch (ignored: Exception) {
                Logger.e(ignored)
            }
        }
    }
}