package xh.rabbit.core.utils

import android.content.Context
import android.util.TypedValue
import android.view.View

class ViewUtil {
    companion object {
        fun setRippleBackground(context: Context?, view: View) {
            if (context == null) return
            val outValue = TypedValue()
            context.theme.resolveAttribute(android.R.attr.selectableItemBackground, outValue, true)
            view.setBackgroundResource(outValue.resourceId)
        }
    }
}