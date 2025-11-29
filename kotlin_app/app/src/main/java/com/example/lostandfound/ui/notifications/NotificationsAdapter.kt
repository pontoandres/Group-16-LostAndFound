package com.example.lostandfound.ui.notifications

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.lostandfound.data.remote.entities.NotificationRemote
import com.example.lostandfound.R
import com.example.lostandfound.model.NotificationUi

class NotificationsAdapter(
    private val data: List<NotificationUi>
) : RecyclerView.Adapter<NotificationsAdapter.VH>() {

    inner class VH(view: View) : RecyclerView.ViewHolder(view) {
        val txtMessage: TextView = view.findViewById(R.id.txtNotification)
        val txtDate: TextView = view.findViewById(R.id.txtDate)
        val highlightBar: View = view.findViewById(R.id.highlightBar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val n = data[position]
        holder.txtMessage.text = n.message
        holder.txtDate.text = n.createdAt.substring(0, 10)

        holder.highlightBar.visibility =
            if (n.highlight) View.VISIBLE else View.INVISIBLE
    }

    override fun getItemCount(): Int = data.size
}

