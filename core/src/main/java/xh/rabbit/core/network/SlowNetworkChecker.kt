package xh.rabbit.core.network

import android.view.View
import xh.rabbit.core.showToast
import xh.rabbit.core.vo.Status

/**
 * 弱网检测
 * 带Loading布局的接口出现一次请求时间超过5s就判定为超时，那么下一次请求时应该显示loading
 * 如果有接口请求时间小于5s，那么判定为正常，那么下一次请求应该隐藏loading
 */
class SlowNetworkChecker private constructor(private val viewContext: View) {

    companion object {
        private const val SLOW_NETWORK_DURATION = 5_000L

        var isSlowNetwork = false

        private var connectionList = HashMap<Any, SlowNetworkChecker>()

        fun check(viewContext: View, status: Status, key: Any) {
            if (status == Status.LOADING) {
//                Log.d("SlowNetworkChecker", "key = ${key.hashCode()}")
                // 如果viewContext相同，即同一个页面，重新创建SlowNetworkChecker
                val checker = SlowNetworkChecker(viewContext)
                checker.recordStart()
                connectionList.put(key, checker)
            } else if (status == Status.SUCCESS || status == Status.ERROR) {
                connectionList.get(key)?.recordEnd()
            }
        }
    }

    private var startTime: Long = 0L
    private var endTime: Long = 0L

    private fun recordStart() {
        startTime = System.currentTimeMillis()
//        Log.d("SlowNetworkChecker", "${viewContext.hashCode()}: start ${startTime}")
        viewContext.postDelayed({
            if (endTime == 0L && connectionList.containsValue(this)) {
                // 对于同一个页面多次加载的情况，如果连接列表还存在对象，那么显示弱网提示
                isSlowNetwork = true
                viewContext.context.showToast("当前网络较慢，请耐心等待")
            }
//            Log.d("SlowNetworkChecker", "${viewContext.hashCode()}: 5s 延迟检测: ${startTime} -> ${endTime}, 对象是否存在列表中: ${connectionList.containsValue(this)}")
        }, SLOW_NETWORK_DURATION)
    }

    private fun recordEnd() {
        endTime = System.currentTimeMillis()
        if (endTime - startTime < SLOW_NETWORK_DURATION) {
            isSlowNetwork = false
        }
//        Log.d("SlowNetworkChecker", "${viewContext.hashCode()}: end ${endTime}, elapse: ${endTime - startTime}, connectionList: ${connectionList.size}")

    }
}