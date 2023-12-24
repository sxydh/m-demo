@file:Suppress("CanBeVal", "KotlinConstantConditions")

package cn.net.bhe.toucheventdemo

import android.content.Context
import android.os.Bundle
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.ComponentActivity

/*
 * 前置说明
 * 1、下述 Event 的坐标都落在 View 或者 ViewGroup 范围内。
 *
 * TouchEvent 的流转
 * 1、主要涉及两个阶段：dispatchTouchEvent 阶段（Activity -> ViewGroup -> View），onTouchEvent 阶段（View -> ViewGroup -> Activity）。
 * 2、流转过程相当于先从根节点 Activity 传播到叶子节点 View，再从叶子节点回溯到根节点，回溯过程触发各节点的 onTouchEvent。
 *
 * TouchEvent 的处理
 * 1、fun onTouchEvent：一般用来处理事件，返回值表示事件是否被消费（是用 true 表示，否用 false 表示）。
 * 2、fun dispatchTouchEvent：该函数默认调用子节点的 dispatchTouchEvent 函数，返回值表示事件是否被消费，默认取 onTouchEvent 函数的返回值（如果 onTouchEvent 返回了 true，但是 dispatchTouchEvent 返回 false，则以后者为准，即事件未被消费，继续向父节点传播。上述情况可能会造成一些不想要的结果，例如：子节点消费了 ACTION_DOWN 事件，并且显示返回 true，但是子节点或者本节点 dispatchTouchEvent 显示返回 false，造成 ACTION_DOWN 实际未被消费，ACTION_MOVE 在本节点无法触发）。一般情况下不要（也不需要） override 本函数。
 * 3、fun onInterceptTouchEvent：该函数用来拦截事件，返回值表示是否拦截事件（是用 true 表示，否用 false 表示）。
 *
 * TouchEvent 的种类（以下只列举常用事件类型）
 * 1、ACTION_DOWN
 * 2、ACTION_MOVE：如果需要在某个节点处理 ACTION_MOVE 事件，则必须先在该节点或子节点消费 ACTION_DOWN 事件。
 * 3、ACTION_UP：如果需要在某个节点处理 ACTION_UP 事件，则必须先在该节点或子节点消费 ACTION_DOWN 事件。
 */

class MainActivity : ComponentActivity() {

    private val tag = this.javaClass.simpleName.padStart(25)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.main_layout)
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag    dispatchTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.dispatchTouchEvent(ev)
        println("[$tag    dispatchTouchEvent]   super.dispatchTouchEvent = $ret}")
        println("[$tag    dispatchTouchEvent]   return = $ret}")
        return ret
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag          onTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.onTouchEvent(ev)
        println("[$tag          onTouchEvent]   super.onTouchEvent(ev) = $ret")
        println("[$tag          onTouchEvent]   return = $ret")
        return ret
    }

}

class FirstLayout : LinearLayout {

    private val tag = this.javaClass.simpleName.padStart(25)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag    dispatchTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.dispatchTouchEvent(ev)
        println("[$tag    dispatchTouchEvent]   super.dispatchTouchEvent = $ret}")
        println("[$tag    dispatchTouchEvent]   return = $ret}")
        return ret
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag onInterceptTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.onInterceptTouchEvent(ev)
        println("[$tag onInterceptTouchEvent]   super.onInterceptTouchEvent = $ret")
        println("[$tag onInterceptTouchEvent]   return = $ret")
        return ret
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag          onTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.onTouchEvent(ev)
        println("[$tag          onTouchEvent]   super.onTouchEvent(ev) = $ret")
        println("[$tag          onTouchEvent]   return = $ret")
        return ret
    }

}

class SecondLayout : LinearLayout {

    private val tag = this.javaClass.simpleName.padStart(25)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag    dispatchTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.dispatchTouchEvent(ev)
        println("[$tag    dispatchTouchEvent]   super.dispatchTouchEvent = $ret}")
        println("[$tag    dispatchTouchEvent]   return = $ret}")
        return ret
    }

    override fun onInterceptTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag onInterceptTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.onInterceptTouchEvent(ev)
        println("[$tag onInterceptTouchEvent]   super.onInterceptTouchEvent = $ret")
        println("[$tag onInterceptTouchEvent]   return = $ret")
        return ret
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag          onTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.onTouchEvent(ev)
        println("[$tag          onTouchEvent]   super.onTouchEvent(ev) = $ret")
        ret = when (ev.action) {
            MotionEvent.ACTION_DOWN -> false
            MotionEvent.ACTION_MOVE -> false
            MotionEvent.ACTION_UP -> false
            else -> false
        }
        println("[$tag          onTouchEvent]   return = $ret")
        return ret
    }

}

class CustomTextView : TextView {

    private val tag = this.javaClass.simpleName.padStart(25)

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs)

    constructor(context: Context, attrs: AttributeSet, defStyleAttr: Int) : super(context, attrs, defStyleAttr)

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag    dispatchTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.dispatchTouchEvent(ev)
        println("[$tag    dispatchTouchEvent]   super.dispatchTouchEvent = $ret}")
        println("[$tag    dispatchTouchEvent]   return = $ret")
        return ret
    }

    override fun onTouchEvent(ev: MotionEvent): Boolean {
        println("[$tag          onTouchEvent]   ${MotionEvent.actionToString(ev.action)}")
        var ret = super.onTouchEvent(ev)
        println("[$tag          onTouchEvent]   super.onTouchEvent(ev) = $ret")
        ret = when (ev.action) {
            MotionEvent.ACTION_DOWN -> true
            MotionEvent.ACTION_MOVE -> false
            MotionEvent.ACTION_UP -> false
            else -> false
        }
        println("[$tag          onTouchEvent]   return = $ret")
        return ret
    }

}