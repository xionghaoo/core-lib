package xh.rabbit.core_lib

import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.View.OnTouchListener
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import eu.chainfire.libsuperuser.Shell
import xh.rabbit.core.utils.SystemUtil


class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        SystemUtil.toFullScreenMode(this)
        setContentView(R.layout.activity_main)

        findViewById<Button>(R.id.btn_test).setOnClickListener {
            Thread {
                if (Shell.SU.available()) {
//                    val exitCode = Shell.Pool.SU.run("echo nobody will ever see this");
                    val exitCode = Shell.Pool.SU.run("input swipe 280 1000 280 100");
                    Log.d(TAG, "onCreate: $exitCode")
                } else {
                    Log.e(TAG, "onCreate: 不支持su", )
                }
            }.start()
        }
    }
}