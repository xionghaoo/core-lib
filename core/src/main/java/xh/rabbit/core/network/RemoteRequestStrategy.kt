package xh.rabbit.core.network

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import xh.rabbit.core.vo.ApiResponse
import xh.rabbit.core.vo.Resource
import java.util.*

/**
 * 纯网络请求策略，无缓存
 */
abstract class RemoteRequestStrategy<ResultType>() {

    private val result: MediatorLiveData<Resource<ResultType>> = MediatorLiveData()

    init {
        initialize()
    }

    private fun initialize() {
        result.value = Resource.loading(null)
        val apiResponse = createCall()
        result.addSource(apiResponse) { response ->
            result.removeSource(apiResponse)
            if (response?.isSuccessful()!!) {
//                onResponse(response)
                setValue(Resource.success(response.body))
            } else {
//                onFetchFailed(response.error!!)
                setValue(Resource.error(response.error!!, response.body))
            }
        }
    }

    @MainThread
    private fun setValue(newValue: Resource<ResultType>) {
        if (!Objects.equals(result.value, newValue)) {
            result.value = newValue
        }
    }

    fun asLiveData(): LiveData<Resource<ResultType>> = result

    @MainThread
    protected abstract fun createCall(): LiveData<ApiResponse<ResultType>>

//    protected abstract fun onFetchFailed(error: String)

    // 该方法提供请求成功时处理响应数据的机会
//    protected abstract fun onResponse(response: ApiResponse<ResultType>)
}