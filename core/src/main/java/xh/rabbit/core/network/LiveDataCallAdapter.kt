package xh.rabbit.core.network

import androidx.lifecycle.LiveData
import retrofit2.Call
import retrofit2.CallAdapter
import retrofit2.Callback
import retrofit2.Response
import xh.rabbit.core.vo.ApiResponse
import java.lang.reflect.Type
import java.util.concurrent.atomic.AtomicBoolean

/**
 * LiveData 转换器，将retrofit的response转换成LiveData<ApiResponse<T>>的形式
 */
class LiveDataCallAdapter<R>(private val type: Type) : CallAdapter<R, LiveData<ApiResponse<R>>> {


    override fun adapt(call: Call<R>): LiveData<ApiResponse<R>> =
        object : LiveData<ApiResponse<R>>() {
            val started: AtomicBoolean = AtomicBoolean(false)

            override fun onActive() {
                super.onActive()
                if (started.compareAndSet(false, true)) {
                    call.enqueue(object : Callback<R> {
                        override fun onResponse(call: Call<R>, response: Response<R>) {
                            postValue(ApiResponse<R>(response))
                        }

                        override fun onFailure(call: Call<R>, t: Throwable) {
                            postValue(ApiResponse<R>(t))
                        }
                    })
                }
            }
        }

    override fun responseType(): Type = type
}