package xh.rabbit.core.vo


class Resource<out T> private constructor(val status: Status, val message: String?, val data: T?) {
    companion object {
        fun <T> success(data: T?) : Resource<T> {
            return Resource(Status.SUCCESS, null, data)
        }

        fun <T> error(msg: String, data: T?) : Resource<T> {
            return Resource(Status.ERROR, msg, data)
        }

        fun <T> loading(data: T?) : Resource<T> {
            return Resource(Status.LOADING, null, data)
        }
    }

    override fun toString(): String {
        return "Resource {status=$status, message=$message, data=$data}";
    }
}