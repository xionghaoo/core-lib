package xh.rabbit.core.widgets

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.util.Log
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import androidx.core.util.remove
import androidx.core.util.set
import androidx.core.view.forEachIndexed
import androidx.core.view.isVisible
import xh.rabbit.core.R
import xh.rabbit.core.vo.Status

/**
 * 网络加载状态布局，可以切换各种结果视图
 * 不要开启动画，切换动画还存在bug
 */
class NetworkStateLayout : FrameLayout {

    companion object {
        private const val TAG = "NetworkStateLayout"

        // loading, error, empty, content
        private val LOADING = arrayOf(true, false, false, false)
        private val ERROR = arrayOf(false, true, false, false)
        private val EMPTY = arrayOf(false, false, true, false)
        private val CONTENT = arrayOf(false, false, false, true)
        private val INITIAL = arrayOf(false, false, false, false)

        // 动画持续时间
        private const val DURATION = 300L
    }

    private var errorViewId: Int = View.NO_ID
    private var loadingViewId: Int = View.NO_ID
    private var emptyViewId: Int = View.NO_ID

    private var errorView: View? = null
    private var emptyView: View? = null
    private var loadingView: View? = null

    private var isAnimShow: Boolean = false

    private var isFirstInflate = true

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {

        var ta: TypedArray? = null

        try {
            ta = context.theme.obtainStyledAttributes(attrs, R.styleable.NetworkStateLayout, 0, 0)
            emptyViewId = ta.getResourceId(R.styleable.NetworkStateLayout_nsl_empty, R.layout.widget_network_default_status_empty)
            errorViewId = ta.getResourceId(R.styleable.NetworkStateLayout_nsl_error, R.layout.widget_network_default_status_error)
            loadingViewId = ta.getResourceId(R.styleable.NetworkStateLayout_nsl_loading, R.layout.widget_network_default_status_loading)

            val inflater = LayoutInflater.from(context)
            errorView = inflater.inflate(errorViewId, this, false)
            emptyView = inflater.inflate(emptyViewId, this, false)
            loadingView = inflater.inflate(loadingViewId, this, false)
            addView(emptyView)
            addView(errorView)
            addView(loadingView)

        } finally {
            ta?.recycle()
        }

        // 不要放在onFinishInflate中，避免触发二次布局
        show(INITIAL)
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
    }

    fun emptyView() = emptyView

    fun setNeedLoadingAnimation(isNeed: Boolean?) {
        isNeed?.let { isFirstInflate = it }
    }

    fun loading() {
        if (isFirstInflate) {
            show(LOADING)
            isFirstInflate = false
        }
    }

    fun success() {
        show(CONTENT)
    }

    fun error() {
        show(ERROR)
    }

    fun empty() {
        show(EMPTY)
    }

    fun networkStatus(status: Status) {
        // 弱网监听
//        SlowNetworkChecker.check(this, status, this)
        when(status) {
            Status.LOADING -> loading()
            Status.SUCCESS -> success()
            Status.ERROR -> error()
        }
    }

    fun isAnimationShow(isShow: Boolean) {
        isAnimShow = isShow

        // 关闭动画时将所有View的透明度还原
        if (!isAnimShow) {
            forEachIndexed { index, view ->
                view.alpha = 1f
            }
        }
    }

    private fun show(index: Array<Boolean>) {
        if (isAnimShow) {
            fadeShow(loadingView, index[0])
            fadeShow(errorView, index[1])
            fadeShow(emptyView, index[2])
            fadeShowContent(index[3])
        } else {
            loadingView?.visibility = isShow(index[0])
            errorView?.visibility = isShow(index[1])
            emptyView?.visibility = isShow(index[2])
            isShowContent(index[3])
        }
    }

    private fun isShow(isShow: Boolean) = if (isShow) View.VISIBLE else View.GONE

    private fun isShowContent(isShow: Boolean) {
        forEachIndexed { index, view ->
            if (index > 2) {
                view.visibility = isShow(isShow)
            }
        }
    }

    private fun fadeShowContent(isShow: Boolean) {
//        Log.d(TAG, "fadeShowContent: $isShow")
        forEachIndexed { index, view ->
            if (index > 2) {
                fadeShow(view, isShow)
            }
        }
    }

    private fun fadeShow(view: View?, isShow: Boolean) {
        view?.also {
            if ((it.isVisible && isShow) || (!it.isVisible && !isShow)) {
                return@also
            }
            it.visibility = View.VISIBLE
            it.animate().cancel()
            if (isShow) {
                it.alpha = 0f
                it.animate()
                    .alpha(1f)
                    .setDuration(DURATION)
                    .start()
            } else {
                it.alpha = 1f
                it.animate()
                    .alpha(0f)
                    .setDuration(DURATION)
                    .withEndAction { it.visibility = View.GONE }
                    .start()
            }
        }

    }

}
