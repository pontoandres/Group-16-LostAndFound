package com.example.lostandfound.ui.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.R
import com.example.lostandfound.model.LostItem
import java.util.Locale

class LostItemAdapter(
    private val onItemClick: (LostItem) -> Unit
) : RecyclerView.Adapter<LostItemAdapter.VH>() {

    private val all = mutableListOf<LostItem>()
    private val data = mutableListOf<LostItem>()

    fun submitList(items: List<LostItem>) {
        all.clear()
        all.addAll(items)
        data.clear()
        data.addAll(items)
        notifyDataSetChanged()
    }

    fun filterBy(query: String) {
        val q = query.trim().lowercase(Locale.getDefault())
        data.clear()
        if (q.isEmpty()) {
            data.addAll(all)
        } else {
            data.addAll(all.filter { it.name.lowercase(Locale.getDefault()).contains(q) })
        }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_lost, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) = holder.bind(data[position])

    override fun getItemCount(): Int = data.size

    inner class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val img: ImageView = itemView.findViewById(R.id.imgItem)
        private val name: TextView = itemView.findViewById(R.id.txtName)

        fun bind(item: LostItem) {
            img.setImageResource(item.imageRes)
            name.text = item.name
            itemView.setOnClickListener { onItemClick(item) }
            name.setOnClickListener { onItemClick(item) }
        }
    }
}
