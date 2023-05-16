package xh.rabbit.core.utils

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.usage.UsageStats
import android.app.usage.UsageStatsManager
import android.content.ActivityNotFoundException
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.Resources
import android.graphics.Color
import android.media.AudioManager
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.provider.Settings
import android.text.method.DigitsKeyListener
import android.util.DisplayMetrics
import android.util.Log
import android.util.Size
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import androidx.annotation.ColorRes
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import java.util.*


class SystemUtil {
    companion object {
        // 隐藏软键盘
        fun hideSoftKeyboard(context: Context, editText: EditText) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(editText.windowToken, 0)
        }

        // 调起软键盘
        fun openSoftKeyboard(context: Context) {
            val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        }

        // 获取状态栏高度(px), android6.0以后 = 24dp
        fun getStatusBarHeight(resources: Resources) : Int {
            var result = 0
            val resourceId = resources.getIdentifier("status_bar_height", "dimen", "android")
            if (resourceId > 0) {
                result = resources.getDimensionPixelSize(resourceId)
            }
            return result
        }

        // 复制文本到剪贴板
//        fun copyToClipboard(context: Context, str: String) {
//            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("Copied Text", str)
//            clipboard.primaryClip = clip
//        }

        fun setImageForegroundColor(img: ImageView, context: Context, color: Int) {
            img.setColorFilter(
                    ContextCompat.getColor(context, color),
                    android.graphics.PorterDuff.Mode.SRC_ATOP
            )
        }

        /**
         * 设置灰色状态栏
         */
        fun setDarkStatusBar(window: Window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = window.decorView
                val flags = decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                decor.systemUiVisibility = flags
            }
        }

        /**
         * 清除灰色状态栏
         */
        fun clearDarkStatusBar(window: Window) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                val decor = window.decorView
                val flags = decor.systemUiVisibility and View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR.inv()
                decor.systemUiVisibility = flags
            }
        }

        fun setStatusBarColor(activity: Activity, window: Window, @ColorRes color: Int) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.statusBarColor = ContextCompat.getColor(activity, color)
            }
        }

        fun statusBarTransparent(window: Window) {
            if (Build.VERSION.SDK_INT >= 19 && Build.VERSION.SDK_INT < 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, true, window)
            }
            if (Build.VERSION.SDK_INT >= 19) {
                val decor = window.decorView
                decor.systemUiVisibility = decor.systemUiVisibility or View.SYSTEM_UI_FLAG_LAYOUT_STABLE or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
            }
            if (Build.VERSION.SDK_INT >= 21) {
                setWindowFlag(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS, false, window)
                window.statusBarColor = Color.TRANSPARENT
            }
        }

        fun getNavigationBarHeight(context: Context) : Int {
            val resources: Resources = context.resources
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }

        private fun setWindowFlag(bits: Int, on: Boolean, window: Window) {
            val win = window
            val winParams = win.attributes
            if (on) {
                winParams.flags = winParams.flags or bits
            } else {
                winParams.flags = winParams.flags and bits.inv()
            }
            win.attributes = winParams
        }

        /**
         * 调起系统电话
         */
        fun call(context: Activity?, phoneNumber: String?) {
            if (context == null || phoneNumber == null) return
            if (hasPermission(context, Manifest.permission.CALL_PHONE)) {
                val intent = Intent(Intent.ACTION_CALL)
                intent.data = Uri.parse("tel:" + phoneNumber)
                context.startActivity(intent)
            }
        }

        /**
         * 调起系统短信
         */
        fun sms(context: Activity?, phone: String?, message: String?) {
            if (context == null || phone == null) return
            if (hasPermission(context, Manifest.permission.SEND_SMS)) {
                val smsToUri = Uri.parse("smsto: " + phone)
                val intent = Intent(Intent.ACTION_SENDTO, smsToUri)
                intent.putExtra("sms_body", message)
                context.startActivity(intent)
            }
        }

        /**
         * 打开设置-当前App权限设置
         */
        fun openSettingsPermission(activity: Activity?) {
            val intent = Intent()
            intent.action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
            val uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            activity?.startActivity(intent)
        }

        /**
         * 打开wifi设置
         */
        fun openSettingsWifi(context: Context?) {
            try {
                val intent = Intent(Intent.ACTION_MAIN, null)
                intent.addCategory(Intent.CATEGORY_LAUNCHER)
                val cn = ComponentName("com.android.settings", "com.android.settings.wifi.WifiSettings")
                intent.component = cn
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                context?.startActivity(intent)
            } catch (ignored: ActivityNotFoundException) {
                context?.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS))
            }
        }

        /**
         * 打开设置-当前App权限设置
         */
        @RequiresApi(Build.VERSION_CODES.M)
        fun openSettingsWrite(activity: Activity?) {
            val intent = Intent()
            intent.action = Settings.ACTION_MANAGE_WRITE_SETTINGS
            val uri = Uri.fromParts("package", activity?.packageName, null)
            intent.data = uri
            activity?.startActivity(intent)
        }

        /**
         * 打开系统设置
         */
        fun openSettings(activity: Activity?) {
            activity?.startActivityForResult(Intent(Settings.ACTION_SETTINGS), 0)
        }

        /**
         * 打开设置-位置服务
         */
        fun openSettingsLocationService(activity: Activity?) {
            activity?.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS))
        }

        fun toFullScreenMode(activity: AppCompatActivity) {
            activity.requestWindowFeature(Window.FEATURE_NO_TITLE);
            activity.window.setFlags(
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
                    WindowManager.LayoutParams.FLAG_FULLSCREEN,
            );
            val flags = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN
                    or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY)
            activity.window.decorView.systemUiVisibility = flags

            activity.window.decorView.setOnSystemUiVisibilityChangeListener { visibility ->
                if (visibility and View.SYSTEM_UI_FLAG_FULLSCREEN == 0) {
                    activity.window.decorView.systemUiVisibility = flags
                }
            }
        }

        /**
         * 限制键盘的输入
         */
        fun limitKeyboardInput(editText: EditText?, allowInput: String = "0123456789") {
            if (editText == null) return
            editText.keyListener = DigitsKeyListener.getInstance(allowInput)
        }

        fun displayInfo(context: Context) : DisplayMetrics {
            val metrics = DisplayMetrics()
            val wm = context.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            wm.defaultDisplay.getMetrics(metrics)
            return metrics
        }

        // 完全沉浸模式
        fun hideSystemUI(window: Window) {
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        // 完全沉浸模式
        fun hideSystemUI(view: View) {
            // Enables regular immersive mode.
            // For "lean back" mode, remove SYSTEM_UI_FLAG_IMMERSIVE.
            // Or for "sticky immersive," replace it with SYSTEM_UI_FLAG_IMMERSIVE_STICKY
            view.systemUiVisibility = (View.SYSTEM_UI_FLAG_IMMERSIVE
                    // Set the content to appear under the system bars so that the
                    // content doesn't resize when the system bars hide and show.
                    or View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    // Hide the nav bar and status bar
                    or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_FULLSCREEN)
        }

        // Shows the system bars by removing all the flags
        // except for the ones that make the content appear under the system bars.
        fun showSystemUI(window: Window) {
            window.decorView.systemUiVisibility = (View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    or View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    or View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN)
        }

        fun disableEditTextKeyboard(context: Context, edt: EditText) {
            edt.setOnTouchListener { v, event ->
                val imm: InputMethodManager = context.applicationContext.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.hideSoftInputFromWindow(v.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
                if (v == edt) {
                    edt.requestFocus()
                }
                true
            }
        }

        fun uninstallApp(context: Context, packageName: String?) {
            if (packageName == null) return
            try {
                val packageURI = Uri.parse("package:$packageName")
                val uninstallIntent = Intent(Intent.ACTION_DELETE, packageURI)
                context.startActivity(uninstallIntent)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun appInfo(context: Context?, packageName: String, clsName: String) {
            val intent = Intent(Intent.ACTION_MAIN)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
            intent.setClassName(packageName, clsName)
            try {
                val str = "android.settings.APPLICATION_DETAILS_SETTINGS"
                val stringBuilder = StringBuilder()
                stringBuilder.append("package:")
                val component = intent.component
                stringBuilder.append(component!!.packageName)
                context?.startActivity(Intent(str, Uri.parse(stringBuilder.toString())))
            } catch (e: java.lang.Exception) {
                e.printStackTrace()
//                Tool.toast(_homeActivity, R.string.toast_app_uninstalled)
            }
        }

        @SuppressLint("MissingPermission")
        fun isNetworkConnected(context: Context?): Boolean {
            if (context == null) return false
            if (hasPermission(context, Manifest.permission.ACCESS_NETWORK_STATE)) {
                val connectivityManager =
                    context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
                val activeNetworkInfo = connectivityManager.activeNetworkInfo
                return activeNetworkInfo != null && activeNetworkInfo.isConnectedOrConnecting
            } else {
                throw IllegalAccessException("缺少 ACCESS_NETWORK_STATE 权限")
            }

        }

        /**
         * 调节系统音量
         */
        fun systemVolumeAdjust(
                context: Context,
                musicIndex: Int = 0,
                ringIndex: Int = 0,
                alarmIndex: Int = 0,
                systemIndex: Int = 0,
                notificationIndex: Int = 0
        ) {
            val mgr = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager?
            mgr?.apply {
                val musicVolume = getStreamVolume(AudioManager.STREAM_MUSIC)
                val ringVolume = getStreamVolume(AudioManager.STREAM_RING)
                val alarmVolume = getStreamVolume(AudioManager.STREAM_ALARM)
                val systemVolume = getStreamVolume(AudioManager.STREAM_SYSTEM)
                val notificationVolume = getStreamVolume(AudioManager.STREAM_NOTIFICATION)

                setStreamVolume(AudioManager.STREAM_MUSIC, musicIndex, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
                setStreamVolume(AudioManager.STREAM_RING, ringIndex, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
                setStreamVolume(AudioManager.STREAM_ALARM, alarmIndex, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
                setStreamVolume(AudioManager.STREAM_SYSTEM, systemIndex, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
                setStreamVolume(AudioManager.STREAM_NOTIFICATION, notificationIndex, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE)
            }
        }

        fun hasPermission(context: Context, permission: String): Boolean {
            val perm = context.checkCallingOrSelfPermission(permission)
            return perm == PackageManager.PERMISSION_GRANTED
        }

        fun getTopAppFromLollipopOnwards(context: Context): String? {
            var topPackageName: String? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val mUsageStatsManager: UsageStatsManager? =
                    context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager?
                val time = System.currentTimeMillis()
                // We get usage stats for the last 10 seconds
                val stats: List<UsageStats>? = mUsageStatsManager?.queryUsageStats(
                    UsageStatsManager.INTERVAL_DAILY,
                    time - 1000 * 10,
                    time
                )
                // Sort the stats by the last time used
                if (stats != null) {
                    val mySortedMap: SortedMap<Long, UsageStats> = TreeMap<Long, UsageStats>()
                    for (usageStats in stats) {
                        mySortedMap.put(usageStats.getLastTimeUsed(), usageStats)
                    }
                    if (!mySortedMap.isEmpty()) {
                        topPackageName = mySortedMap.get(mySortedMap.lastKey())?.getPackageName()
                        Log.e("SystemUtil", "TopPackage Name ${topPackageName}")
                    }
                }
            }
            return topPackageName
        }

        private fun hasUsageAccessSettingsOption(context: Context): Boolean {
            return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                val packageManager: PackageManager = context.packageManager
                val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS)
                val list: List<ResolveInfo> =
                    packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
                list.isNotEmpty()
            } else {
                false
            }
        }

        private fun isUsageStatsServiceOpen(context: Context): Boolean {
            var queryUsageStats: List<UsageStats?>? = null
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1) {
                val usageStatsManager: UsageStatsManager =
                    context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
                queryUsageStats = usageStatsManager.queryUsageStats(
                    UsageStatsManager.INTERVAL_BEST,
                    0,
                    System.currentTimeMillis()
                )
            }
            return !(queryUsageStats == null || queryUsageStats.isEmpty())
        }

        /**
         * 获取状态栏高度
         */
        fun getNavigationBarHeight(resources: Resources): Int {
            val resourceId = resources.getIdentifier("navigation_bar_height", "dimen", "android")
            return if (resourceId > 0) {
                resources.getDimensionPixelSize(resourceId)
            } else 0
        }

        /**
         * 获取完整的屏幕尺寸
         */
        fun screenSize(context: Context, isWidthAttach: Boolean = false) : Size {
            val displayInfo = displayInfo(context)
            val navHeight = getStatusBarHeight(context.resources)
            val width = if (isWidthAttach) displayInfo.widthPixels + navHeight else displayInfo.widthPixels
            val height = if (isWidthAttach) displayInfo.heightPixels else displayInfo.heightPixels + navHeight
            return Size(width, height)
        }

    }
}