package com.example.lostandfound.ui.history

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.model.HistoryItem

class HistoryAdapter(
    private val items: List<HistoryItem>
) : RecyclerView.Adapter<HistoryAdapter.VH>() {

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val title: TextView = itemView.findViewById(R.id.tvEventTitle)
        private val date: TextView = itemView.findViewById(R.id.tvEventDate)
        private val objectInfo: TextView = itemView.findViewById(R.id.tvObjectInfo)
        private val link: TextView = itemView.findViewById(R.id.tvLink)

        fun bind(item: HistoryItem) {
            title.text = item.title
            date.text = item.date
            objectInfo.text = item.objectInfo

            if (item.link.isNullOrEmpty()) {
                link.visibility = View.GONE
            } else {
                link.text = item.link
                link.visibility = View.VISIBLE
                // Optional: click handler for "link to post"
                link.setOnClickListener {
                    // TODO: open detail screen for the post
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(items[position])

    override fun getItemCount(): Int = items.size
}