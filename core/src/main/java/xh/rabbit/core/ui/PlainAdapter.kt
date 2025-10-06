package xh.rabbit.core.ui

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import xh.rabbit.core.setDebouncedClickListener

abstract class PlainAdapter<T>(
    private val _items: ArrayList<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var listener: ((T, Int) -> Unit)? = null

    var selection = 0

    private class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(itemLayoutId(), parent, false))
    }

    override fun getItemCount(): Int = _items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = _items[position]
        val v = holder.itemView
        listener?.let { click ->
            (clickView(v) ?: v).setDebouncedClickListener {
                click(item, position)
            }
        }
        bindView(v, item, position)
    }

    protected fun isSelected(position: Int) = position == selection

    protected fun notifySelection(position: Int) {
        if (selection != position) {
            val pos = selection
            selection = position
            notifyItemChanged(selection)
            notifyItemChanged(pos)
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    open fun updateData(data: ArrayList<T>) {
        _items.clear()
        _items.addAll(data)
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<T> = _items
    open fun clickView(parent: View): View? = null

    abstract fun itemLayoutId(): Int

    abstract fun bindView(v: View, item: T, position: Int)

    fun setOnItemClickListener(onClick: (T, Int) -> Unit) {
        listener = onClick
    }
}