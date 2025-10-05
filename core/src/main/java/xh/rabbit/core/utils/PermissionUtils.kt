package xh.rabbit.core.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment

object PermissionUtils {
    fun isPermissionGranted(context: Context, permission: String): Boolean {
        val result: Int = ContextCompat.checkSelfPermission(context, permission)
        return result == PackageManager.PERMISSION_GRANTED
    }

    fun getUngrantedPermissions(context: Context, permissions: Array<String>): List<String> {
        val ungranted: List<String> = permissions.filter { perm: String ->
            ContextCompat.checkSelfPermission(context, perm) != PackageManager.PERMISSION_GRANTED
        }
        return ungranted
    }

    fun shouldShowRationale(fragment: Fragment, permissions: Array<String>): Boolean {
        val needsRationale: Boolean = permissions.any { perm: String ->
            fragment.shouldShowRequestPermissionRationale(perm)
        }
        return needsRationale
    }

    fun openAppSettings(context: Context) {
        val intent: Intent = Intent(
            Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", context.packageName, null)
        )
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}