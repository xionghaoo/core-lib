package xh.rabbit.core.utils

import android.view.View

/**
 * 防止重复点击策略
 */
class OnceClickStrategy private constructor() {

    companion object {
        private var lastClickTime = 0L
        private var CLICK_INTERVAL = 1000L

        fun onceClick(v: View, clickCallback: (v: View) -> Unit) {
            v.setOnClickListener {
                if (System.currentTimeMillis() - lastClickTime > CLICK_INTERVAL) {
                    lastClickTime = System.currentTimeMillis()
                    clickCallback(v)
                } else {
//                    ToastUtil.show(v.context, "点击频率过快")
                }
            }
        }
    }

}