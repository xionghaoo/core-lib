package xh.rabbit.core.network


import okhttp3.Interceptor
import okhttp3.MediaType
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody

/**
 * retrofit Api Mock
 * mock uri end with "_mock"
 */
class MockInterceptor(private val isDebug: Boolean, private val mockResponseJson: String) :
    Interceptor {

    companion object {
        private const val SUCCESS_CODE = 200
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        if (isDebug) {
            val uri = chain.request().url().uri().toString()
            return if (uri.endsWith("_mock")) {
                chain.proceed(chain.request())
                    .newBuilder()
                    .code(SUCCESS_CODE)
                    .protocol(Protocol.HTTP_2)
                    .message(mockResponseJson)
                    .body(
                        ResponseBody.create(
                            MediaType.parse("application/json"),
                            mockResponseJson.toByteArray()
                        )
                    )
                    .addHeader("content-type", "application/json")
                    .build()
            } else {
                chain.proceed(chain.request())
            }
        } else {
            //just to be on safe side.
            throw IllegalAccessError(
                "MockInterceptor is only meant for Testing Purposes and " +
                    "bound to be used only with DEBUG mode"
            )
        }
    }

}