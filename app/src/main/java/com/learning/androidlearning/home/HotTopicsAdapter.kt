package com.learning.androidlearning.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.learning.androidlearning.R

class HotTopicsAdapter : RecyclerView.Adapter<HotTopicsAdapter.ViewHolder>() {

    private val items = mutableListOf<HotTopic>()

    fun setItems(newItems: List<HotTopic>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view =
                LayoutInflater.from(parent.context).inflate(R.layout.item_hot_topic, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(items[position])
    }

    override fun getItemCount(): Int = items.size

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvTitle)

        fun bind(item: HotTopic) {
            tvTitle.text = item.title
        }
    }
}

data class HotTopic(val id: String, val title: String)
