package cn.net.bhe.recyclerviewdemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.DOWN
import androidx.recyclerview.widget.ItemTouchHelper.LEFT
import androidx.recyclerview.widget.ItemTouchHelper.UP
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.list)
        val recyclerView: RecyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = RecyclerViewAdapter(mutableListOf("Hello", "World"))

        // 列表向支持位置拖动，左滑删除。
        val callBack = CallBack(recyclerView.adapter as RecyclerViewAdapter)
        val itemTouchHelper = ItemTouchHelper(callBack)
        itemTouchHelper.attachToRecyclerView(recyclerView)
    }
}

class CallBack(private val rcv: RecyclerViewAdapter) : ItemTouchHelper.Callback() {

    /* 滑动超过一半才算移除 */
    override fun getMoveThreshold(viewHolder: RecyclerView.ViewHolder): Float {
        return .5F
    }

    /* 滑动速率设置最大，避免误操作。 */
    override fun getSwipeEscapeVelocity(defaultValue: Float): Float {
        return Float.MAX_VALUE
    }

    override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
        // 可以上下拖动，左滑。
        return makeMovementFlags(UP or DOWN, LEFT)
    }

    override fun onMove(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder, target: RecyclerView.ViewHolder): Boolean {
        rcv.onMove(viewHolder.adapterPosition, target.adapterPosition)
        return true
    }

    override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
        rcv.onSwiped(viewHolder.adapterPosition)
    }

}
