package xh.rabbit.core.ui

import android.os.SystemClock
import android.view.View

internal const val DEFAULT_CLICK_INTERVAL_MS: Long = 600L

class DebouncedOnClickListener(
    private val intervalMs: Long = DEFAULT_CLICK_INTERVAL_MS,
    private val onSingleClick: (View) -> Unit
) : View.OnClickListener {
    private var lastClickUptimeMs: Long = 0L
    override fun onClick(v: View): Unit {
        val now: Long = SystemClock.uptimeMillis()
        if (now - lastClickUptimeMs < intervalMs) return
        lastClickUptimeMs = now
        onSingleClick(v)
    }
}