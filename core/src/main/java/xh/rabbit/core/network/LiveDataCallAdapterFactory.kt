package xh.rabbit.core.network


import androidx.lifecycle.LiveData
import retrofit2.CallAdapter
import retrofit2.Retrofit
import xh.rabbit.core.network.LiveDataCallAdapter
import xh.rabbit.core.vo.ApiResponse
import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type

class LiveDataCallAdapterFactory : CallAdapter.Factory() {
    override fun get(
        returnType: Type,
        annotations: Array<out Annotation>,
        retrofit: Retrofit
    ): CallAdapter<*, *>? {
        if (getRawType(returnType) != LiveData::class.java) {
            return null
        }
        val observableType = getParameterUpperBound(0, returnType as ParameterizedType)
        val rowObservableType = getRawType(observableType)
        if (rowObservableType != ApiResponse::class.java) {
            throw IllegalArgumentException("type must be a resource")
        }
        if (!(observableType is ParameterizedType)) {
            throw IllegalArgumentException("resource must be parameterized")
        }
        val bodyType: Type = getParameterUpperBound(0, observableType)
        return LiveDataCallAdapter<Any>(bodyType)
    }
}