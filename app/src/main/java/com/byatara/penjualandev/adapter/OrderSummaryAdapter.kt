package com.byatara.penjualandev.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelOrderItem
import java.text.NumberFormat
import java.util.Locale

class OrderSummaryAdapter(
    private val itemList: List<ModelOrderItem>
) : RecyclerView.Adapter<OrderSummaryAdapter.SummaryViewHolder>() {

    private lateinit var context: Context

    inner class SummaryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_summary_name)
        val tvQtyPrice: TextView = itemView.findViewById(R.id.tv_summary_qty_price)
        val tvSubtotal: TextView = itemView.findViewById(R.id.tv_summary_subtotal)

        fun bind(item: ModelOrderItem) {
            tvName.text = item.namaProduk ?: "-"
            tvQtyPrice.text = "${item.qty} x ${formatRupiah(item.hargaJual ?: 0)}"
            tvSubtotal.text = formatRupiah(item.subtotal ?: 0)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SummaryViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_order_summary, parent, false)
        return SummaryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SummaryViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int = itemList.size
}
