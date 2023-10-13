package xh.rabbit.core

import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.Settings
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import pub.devrel.easypermissions.AfterPermissionGranted
import pub.devrel.easypermissions.EasyPermissions

abstract class PermissionActivity : AppCompatActivity() {

    companion object {
        private const val RD_PERMISSION = 1
    }

    private val activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
        requestPermissions()
    }

    /**
     * Request user permissions
     * 申请权限
     */
    protected fun requestUserPermissions() {
        // 申请权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            if (!Environment.isExternalStorageManager()) {
                try {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:$packageName"))
                    activityLauncher.launch(intent)
                } catch (e: Exception) {
                    val intent = Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION)
                    activityLauncher.launch(intent)
                }
            } else {
                requestPermissions()
            }
        } else {
            requestPermissions()
        }
    }

    abstract fun onPermissionGranted()

    abstract fun needPermissions() : Array<String>

    abstract fun permissionReceivers() : Array<Any>

    protected fun permissionPrompt(): String = "App需要相关权限，请授予"

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, *permissionReceivers())
    }

    @AfterPermissionGranted(RD_PERMISSION)
    private fun requestPermissions() {
        if (hasPermission()) {
            onPermissionGranted()
        } else {
            EasyPermissions.requestPermissions(
                this,
                permissionPrompt(),
                RD_PERMISSION,
                *needPermissions()
            )
        }
    }

    private fun hasPermission(): Boolean {
        return EasyPermissions.hasPermissions(this, *needPermissions())
    }
}