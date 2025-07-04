package xh.rabbit.core.utils

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import xh.rabbit.core.data.AppMetaInfo
import java.io.File

object ApkUtil {
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

    fun appList(activity: Context) : List<AppMetaInfo> {
        val appList = ArrayList<AppMetaInfo>()
        // 获取已安装的app列表
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_LAUNCHER)
        activity.packageManager.apply {
            val packageList = queryIntentActivities(intent, 0)
            packageList.forEach { info ->
                val appInfo: ApplicationInfo = getApplicationInfo(info.activityInfo.packageName, 0)
                val appName = getApplicationLabel(appInfo).toString()
                val icon = getApplicationIcon(appInfo)
                appList.add(AppMetaInfo(appInfo.packageName, appName, icon))
            }
        }
        return appList
    }

    fun appIsInstalled(context: Context, pkgName: String?): Boolean {
        if (pkgName.isNullOrEmpty()) return false
        return try {
            context.packageManager.getPackageInfo(pkgName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            Log.e("ApkUtil", "error: $e")
            false
        }
    }

    fun startLauncherApp(context: Context, pkgName: String) {
        context.packageManager.apply {
            val launchIntent = getLaunchIntentForPackage(pkgName)
            context.startActivity(launchIntent)
        }
    }

    fun startApp(context: Context, pkgName: String, startPage: String) {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.setClassName(pkgName, startPage)
        context.startActivity(intent)
    }
}