package cn.net.bhe.sketchdemo.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.view.MotionEvent
import android.view.View
import kotlin.random.Random

class CanvasView(context: Context) : View(context) {

    /* 绘制路径 */
    private var path: Path = Path()

    /* 绘制画笔 */
    private var paint: Paint = Paint()

    /* 已经绘制的位图 */
    private var hisBitmap: Bitmap? = null

    /* 已经绘制的 Canvas */
    private var hisCanvas: Canvas? = null

    /*
     * 已经绘制的位图的画笔
     * 当绘制颜色较少的位图或在某些情况下绘制渐变时，启用抖动可以改善颜色的过渡，使颜色看起来更加平滑和自然。
     */
    private var hisPaint: Paint = Paint(Paint.DITHER_FLAG)

    /* 绘制画笔的随机颜色 */
    private val colors: Array<Int> = arrayOf(Color.BLACK, Color.BLUE, Color.YELLOW)
    private val random: Random = Random(System.currentTimeMillis())

    init {
        setupDrawing()
    }

    private fun setupDrawing() {
        /* 画笔颜色 */
        paint.color = Color.BLACK
        /* 画笔样式设置为描边 */
        paint.style = Paint.Style.STROKE
        paint.strokeJoin = Paint.Join.ROUND
        paint.strokeCap = Paint.Cap.ROUND
        /* 画笔宽度 */
        paint.strokeWidth = 5f
        /* 启用抗锯齿 */
        paint.isAntiAlias = true
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        hisBitmap = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
        hisCanvas = Canvas(hisBitmap!!)
    }

    override fun onDraw(canvas: Canvas) {
        /* onDraw 调用时 canvas 会被完全重新绘制，也就是之前的内容会被清空，需要借助 hisBitmap 将之前已经绘制的内容进行重新填充,再绘制当前的路径。 */
        canvas.drawBitmap(hisBitmap!!, 0f, 0f, hisPaint)
        canvas.drawPath(path, paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val touchX = event.x
        val touchY = event.y

        when (event.action) {
            MotionEvent.ACTION_DOWN -> path.moveTo(touchX, touchY)
            MotionEvent.ACTION_MOVE -> path.lineTo(touchX, touchY)
            MotionEvent.ACTION_UP -> {
                /* 画笔抬起时，需要将已经绘制的内容保存到 hisCanvas 当中，便于 onDraw 时进行重新填充。 */
                hisCanvas!!.drawPath(path, paint)
                /* 重置画笔 */
                path.reset()
                paint.color = colors[random.nextInt(colors.size)]
            }

            else -> return false
        }
        /* 触发 onDraw */
        invalidate()
        return true
    }
}