package cn.net.bhe.recyclerviewdemo

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.Adapter
import androidx.recyclerview.widget.RecyclerView.ViewHolder
import java.util.Collections

class RecyclerViewAdapter(private val list: MutableList<String>) : Adapter<ViewHolder>() {

    class ItemViewHolder(private val view: View) : ViewHolder(view) {

        fun onBind(string: String) {
            val textView: TextView = view.findViewById(R.id.textView)
            textView.text = string
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.item, parent, false)
        return ItemViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return list.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder as ItemViewHolder
        holder.onBind(list[position])
    }

    fun onMove(from: Int, to: Int) {
        Collections.swap(list, from, to)
        notifyDataSetChanged()
    }

    fun onSwiped(position: Int) {
        list.removeAt(position)
        notifyDataSetChanged()
    }

}