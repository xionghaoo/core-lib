package xh.rabbit.core.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class PlainAdapter<T>(
    private var _items: ArrayList<T>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    var selection = 0

    class ItemViewHolder(v: View) : RecyclerView.ViewHolder(v)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return ItemViewHolder(inflater.inflate(itemLayoutId(), parent, false))
    }

    override fun getItemCount(): Int = _items.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val item = _items[position]
        val v = holder.itemView
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

    open fun updateData(data: ArrayList<T>) {
        _items = data
        notifyDataSetChanged()
    }

    fun getData(): ArrayList<T> = _items

    abstract fun itemLayoutId(): Int

    abstract fun bindView(v: View, item: T, position: Int)
}