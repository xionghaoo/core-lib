package xh.rabbit.core.utils

import android.content.Intent
import android.view.View
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity

class FileSelector(
    private val context: AppCompatActivity,
    private val onResult: (path: String?) -> Unit
) {

    private val fileSelectRequest = context.registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        Logger.d("${result.resultCode}")
        val uri = result.data?.data
        Logger.d("uri: ${uri}")
        if (uri == null) {
            onResult(null)
        } else {
            val f = GetFilePathFromUri.getFileAbsolutePath(context, uri)
            onResult(f)
        }
    }

    fun select(type: String) {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = type
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        fileSelectRequest.launch(intent)
    }
}