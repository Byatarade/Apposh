package com.byatara.penjualandev.adapter

import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelNotification
import com.google.android.material.card.MaterialCardView

class NotificationAdapter(private var list: List<ModelNotification>) :
    RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {

    private var onItemClickListener: ((ModelNotification) -> Unit)? = null

    fun setOnItemClickListener(listener: (ModelNotification) -> Unit) {
        onItemClickListener = listener
    }

    fun updateData(newList: List<ModelNotification>) {
        list = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_notification, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = list[position]
        holder.bind(item)
    }

    override fun getItemCount(): Int = list.size

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tv_notif_title)
        private val tvMessage: TextView = itemView.findViewById(R.id.tv_notif_message)
        private val tvTime: TextView = itemView.findViewById(R.id.tv_notif_time)
        private val ivIcon: ImageView = itemView.findViewById(R.id.iv_notif_icon)
        private val cvIconContainer: MaterialCardView = itemView.findViewById(R.id.cv_notif_icon_container)
        private val viewUnread: View = itemView.findViewById(R.id.view_unread_indicator)

        fun bind(item: ModelNotification) {
            tvTitle.text = item.title
            tvMessage.text = item.message
            
            val timeAgo = DateUtils.getRelativeTimeSpanString(
                item.timestamp ?: System.currentTimeMillis(),
                System.currentTimeMillis(),
                DateUtils.MINUTE_IN_MILLIS
            )
            tvTime.text = timeAgo

            // Style based on type
            when (item.type) {
                "transaksi" -> {
                    ivIcon.setImageResource(R.drawable.transaksi)
                    cvIconContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.colorSecondary))
                    ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorPrimary))
                }
                "stok" -> {
                    ivIcon.setImageResource(R.drawable.label)
                    cvIconContainer.setCardBackgroundColor(android.graphics.Color.parseColor("#FEF3C7")) // Light yellow
                    ivIcon.setColorFilter(android.graphics.Color.parseColor("#D97706")) // Amber
                }
                else -> {
                    ivIcon.setImageResource(R.drawable.notif)
                    cvIconContainer.setCardBackgroundColor(ContextCompat.getColor(itemView.context, R.color.gray_background))
                    ivIcon.setColorFilter(ContextCompat.getColor(itemView.context, R.color.colorThird))
                }
            }

            viewUnread.visibility = if (item.isRead == true) View.GONE else View.VISIBLE
            
            itemView.setOnClickListener {
                onItemClickListener?.invoke(item)
            }
        }
    }
}
