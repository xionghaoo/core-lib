package xh.rabbit.core.paging

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.ConcatAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * 分页适配器
 */
abstract class PagingLoadAdapter<T: Any>(
    diffCallback: DiffUtil.ItemCallback<T>
) : PagingDataAdapter<T, RecyclerView.ViewHolder>(diffCallback) {

    /**
     * 内容项视图
     */
    abstract val itemLayoutId: Int

    /**
     * 加载状态视图
     */
    abstract val tailLayoutId: Int

    /**
     * 加载状态适配器
     */
    private val footerStateAdapter: FooterStateAdapter by lazy {
        FooterStateAdapter(
            tailLayoutId = tailLayoutId,
            onBindTailView = ::onBindTailView
        )
    }

    /**
     * 带加载状态的组合适配器
     */
    fun withLoadStateAdapter(): ConcatAdapter = withLoadStateFooter(footerStateAdapter)

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindItemView(holder.itemView, getItem(position), position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(itemLayoutId, parent, false))
    }

    abstract fun onBindItemView(v: View, item: T?, position: Int)

    abstract fun onBindTailView(v: View, state: LoadMoreState)

    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v)
}

private class TailViewHolder(v: View) : RecyclerView.ViewHolder(v)

/**
 * 加载中状态适配器，和主适配器组合，显示<加载中>、<加载完成>、<加载错误>的状态
 */
private class FooterStateAdapter(
    private val tailLayoutId: Int,
    private val onBindTailView: (v: View, state: LoadMoreState) -> Unit,
) : LoadStateAdapter<TailViewHolder>() {

    override fun onBindViewHolder(holder: TailViewHolder, loadState: LoadState) {
        val itemView = holder.itemView
        when(loadState) {
            is LoadState.Loading -> onBindTailView(itemView, LoadMoreState.LOADING)
            is LoadState.Error -> onBindTailView(itemView, LoadMoreState.ERROR)
            is LoadState.NotLoading -> {
                if (loadState.endOfPaginationReached) onBindTailView(itemView, LoadMoreState.LOADED)
            }
            else -> {}
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): TailViewHolder {
        val v = LayoutInflater.from(parent.context).inflate(tailLayoutId, parent, false)
        return TailViewHolder(v)
    }

    override fun displayLoadStateAsItem(loadState: LoadState): Boolean {
        // 除了初始化状态返回false，其他状态都返回true
        return !(loadState is LoadState.NotLoading && !loadState.endOfPaginationReached)
    }

}

enum class LoadMoreState {
    LOADED, LOADING, ERROR
}