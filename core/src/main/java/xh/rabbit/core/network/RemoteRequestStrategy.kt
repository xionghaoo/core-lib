package xh.rabbit.core.network

import androidx.annotation.MainThread
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import xh.rabbit.core.vo.ApiResponse
import xh.rabbit.core.vo.Resource
import java.util.*

/**
 * 转换器
 * ApiResponse -> Resource
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
                setValue(Resource.success(response.body))
            } else {
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
}