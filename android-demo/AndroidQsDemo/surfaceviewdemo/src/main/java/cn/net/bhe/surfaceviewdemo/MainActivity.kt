package cn.net.bhe.surfaceviewdemo

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.SurfaceHolder
import androidx.activity.ComponentActivity
import androidx.annotation.RequiresApi

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.surface_view)
    }
}

@RequiresApi(Build.VERSION_CODES.O)
class SurfaceView : android.view.SurfaceView, SurfaceHolder.Callback, Runnable {

    private val tag = this.javaClass.simpleName
    private val paint = Paint()
    private val path = Path()
    private var x = 0F
    private var y = 0F

    init {
        holder.addCallback(this)
        focusable = FOCUSABLE
        isFocusableInTouchMode = true
        keepScreenOn = true

        paint.style = Paint.Style.STROKE
        paint.strokeWidth = 5F
        paint.color = Color.GREEN
    }

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun surfaceCreated(holder: SurfaceHolder) {
        Thread(this).start()
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {

    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun run() {
        while (true) {
            var canvas: Canvas? = null
            try {
                canvas = holder.lockCanvas()
                canvas.drawColor(Color.TRANSPARENT, PorterDuff.Mode.CLEAR)
                canvas.drawPath(path, paint)
            } catch (e: Exception) {
                Log.e(tag, e.localizedMessage)
            } finally {
                holder.unlockCanvasAndPost(canvas)
            }
        }
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        when (ev.action) {
            MotionEvent.ACTION_DOWN -> {
                path.moveTo(ev.x, ev.y)
                x = ev.x
                y = ev.y
            }

            MotionEvent.ACTION_MOVE -> {
                path.quadTo(x, y, ev.x, ev.y)
                x = ev.x
                y = ev.y
            }
        }
        return true
    }

}