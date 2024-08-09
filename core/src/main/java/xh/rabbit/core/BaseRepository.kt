package xh.rabbit.core

import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.LiveData
import xh.rabbit.core.network.RemoteRequestStrategy
import xh.rabbit.core.vo.ApiResponse
import xh.rabbit.core.vo.Resource
import xh.rabbit.core.vo.Status
import xh.rabbit.core.widgets.NetworkStateLayout

open class BaseRepository

/**
 * 网络请求策略
 *
 * 对于Repository里面的load方法, 可以写成下面的形式
 * ```
 * fun loadFunction() = networkRequestStrategy { function() : LiveData<ApiResponse<T>> }
 * ```
 */
inline fun <T> BaseRepository.remoteRequestStrategy(
    crossinline f: () -> LiveData<ApiResponse<T>>
) = object : RemoteRequestStrategy<T>() {
    override fun createCall(): LiveData<ApiResponse<T>> = f()
}.asLiveData()

inline fun <T> Resource<T>.handleStatus(
    loadingDialog: AlertDialog? = null,
    networkLayout: NetworkStateLayout? = null,
    success: (T?) -> Unit,
    failure: (String?) -> Unit
) {
    networkLayout?.networkStatus(status)
    if (status == Status.LOADING) {
        loadingDialog?.show()
    } else {
        loadingDialog?.dismiss()
    }
    when (status) {
        Status.LOADING -> {

        }
        Status.SUCCESS -> {
            success(data)
        }
        Status.ERROR -> {
            failure(message)
        }
    }
}