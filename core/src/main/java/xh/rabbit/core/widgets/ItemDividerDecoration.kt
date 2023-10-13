package xh.rabbit.core.widgets

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Rect
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView

class ItemDividerDecoration(
    private val lineHeight: Int,
    @ColorInt private val lineColor: Int,
    private val padding: Float = 0f,
    private val ignoreLastChildNum: Int = 1
) : RecyclerView.ItemDecoration() {

    private val paint = Paint()

    init {
        paint.color = lineColor
        paint.isAntiAlias = true
    }

    override fun onDraw(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
        super.onDraw(c, parent, state)
        parent.children.forEachIndexed { index, child ->
            if (index < (parent.childCount - ignoreLastChildNum)) {
                val childLp: RecyclerView.LayoutParams = child.layoutParams as RecyclerView.LayoutParams
                val top = child.bottom + childLp.bottomMargin
                c.drawRect(padding, top.toFloat(), parent.measuredWidth.toFloat() - padding, top + lineHeight.toFloat(), paint)
            }
        }
    }

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        super.getItemOffsets(outRect, view, parent, state)
        outRect.set(0, 0, 0, lineHeight)
    }
}