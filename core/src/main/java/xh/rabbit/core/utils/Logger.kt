package xh.rabbit.core.utils

import android.util.Log

internal class Logger {

    companion object {

        const val TAG = "CORE_LIB"

        fun v(msg: String) {
            Log.v(TAG, msg)
        }

        fun d(msg: String) {
            Log.d(TAG, msg)
        }

        fun i(msg: String) {
            Log.i(TAG, msg)
        }

        fun e(msg: Throwable) {
            Log.e(TAG, msg.localizedMessage ?: "")
        }

    }
}