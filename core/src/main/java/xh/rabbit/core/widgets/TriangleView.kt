package xh.rabbit.core.widgets

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.View
import androidx.annotation.ColorRes
import xh.rabbit.core.R

class TriangleView : View {
    companion object {
        private var WRAP_WIDTH = 120
        private var WRAP_HEIGHT = 60
    }

    enum class Direction {
        LEFT, RIGHT, TOP, BOTTOM
    }

    var lineColor: Int = -1
    var fillColor: Int = -1

    private lateinit var path: Path
    private lateinit var paint: Paint

    private lateinit var pathFill: Path
    private lateinit var paintFill: Paint

    var lineWidth = 3f
    var direction: Direction = Direction.BOTTOM


    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        var ta: TypedArray? = null
        try {
            ta = context.theme.obtainStyledAttributes(attrs, R.styleable.TriangleView, 0, 0)
            fillColor = ta.getColor(R.styleable.TriangleView_triangleColor, resources.getColor(android.R.color.white))
            lineColor = ta.getColor(R.styleable.TriangleView_triangleStrokeColor, resources.getColor(android.R.color.white))
            direction = Direction.values()[ta.getInt(R.styleable.TriangleView_triangleDirection, 3)]
            lineWidth = ta.getDimension(R.styleable.TriangleView_triangleStrokeWidth, 3f)
            init()
        } finally {
            ta?.recycle()
        }
    }

    private fun init() {
        path = Path()
        pathFill = Path()

        paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = lineColor
        paint.style = Paint.Style.FILL_AND_STROKE

        paintFill = Paint(Paint.ANTI_ALIAS_FLAG)
        paintFill.color = fillColor
        paintFill.style = Paint.Style.FILL_AND_STROKE
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val widthSize = MeasureSpec.getSize(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val heightSize = MeasureSpec.getSize(heightMeasureSpec)
        if (widthMode == MeasureSpec.AT_MOST && heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WRAP_WIDTH, WRAP_HEIGHT)
        } else if (widthMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(WRAP_WIDTH, heightSize)
        } else if (heightMode == MeasureSpec.AT_MOST) {
            setMeasuredDimension(widthSize, WRAP_HEIGHT)
        } else {
            setMeasuredDimension(widthSize, heightSize)
        }
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        val topPadding = paddingTop
        val bottomPadding = paddingBottom
        val leftPadding = paddingLeft
        val rightPadding = paddingRight
        val width = (width - leftPadding - rightPadding).toFloat()
        val height = (height - topPadding - bottomPadding).toFloat()

        when(direction) {
            Direction.BOTTOM -> {
                pathFill.reset()
                pathFill.moveTo(lineWidth, 0f)
                pathFill.lineTo(width / 2f, height - lineWidth)
                pathFill.lineTo(width - lineWidth, 0f)
                pathFill.close()

                path.reset()
                path.lineTo(width / 2f, height)
                path.lineTo(width, 0f)
                path.lineTo(width - lineWidth, 0f)
                path.lineTo(width / 2f, height - lineWidth)
                path.lineTo(lineWidth, 0f)
                path.close()

                canvas?.drawPath(pathFill, paintFill)
                canvas?.drawPath(path, paint)
            }
            Direction.TOP -> {
                pathFill.reset()
                pathFill.moveTo(lineWidth, height)
                pathFill.lineTo(width / 2f, lineWidth)
                pathFill.lineTo(width - lineWidth, height)
                pathFill.lineTo(lineWidth, height)
                pathFill.close()

                path.reset()
                path.lineTo(0f, height)
                path.lineTo(width / 2f, 0f)
                path.lineTo(width, height)
                path.lineTo(width - lineWidth, height)
                path.lineTo(width / 2f, lineWidth)
                path.lineTo(lineWidth, height)
                path.lineTo(0f, height)
                path.close()

                canvas?.drawPath(pathFill, paintFill)
                canvas?.drawPath(path, paint)
            }
            else -> {}
        }

    }

    fun initial(
        @ColorRes lineColor: Int,
        @ColorRes fillColor: Int,
        width: Float,
        d: Direction
    ) {
        paint.color = resources.getColor(lineColor)
        paintFill.color = resources.getColor(fillColor)
        lineWidth = width
        direction = d
        postInvalidate()
    }
}