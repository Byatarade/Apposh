package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelOrder
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class PrintHistoryAdapter(
    private var orderList: List<ModelOrder>,
    private val onPrintClick: (ModelOrder) -> Unit
) : RecyclerView.Adapter<PrintHistoryAdapter.PrintHistoryViewHolder>() {

    fun updateData(newList: List<ModelOrder>) {
        orderList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PrintHistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_print_history, parent, false)
        return PrintHistoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: PrintHistoryViewHolder, position: Int) {
        holder.bind(orderList[position], onPrintClick)
    }

    override fun getItemCount(): Int = orderList.size

    class PrintHistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvOrderStaff: TextView = itemView.findViewById(R.id.tv_order_staff)
        private val tvOrderMethod: TextView = itemView.findViewById(R.id.tv_order_method)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        private val btnPrint: MaterialButton = itemView.findViewById(R.id.btn_print_item)

        fun bind(order: ModelOrder, onPrintClick: (ModelOrder) -> Unit) {
            tvOrderId.text = order.idOrder ?: "-"
            tvOrderDate.text = order.tanggalWaktu ?: "-"

            val kasir = order.namaKasir ?: "Kasir Utama"
            val pelanggan = if (order.namaPelanggan.isNullOrEmpty()) "Pelanggan Umum" else order.namaPelanggan
            tvOrderStaff.text = "Kasir: $kasir | Pelanggan: $pelanggan"

            tvOrderMethod.text = order.metodeBayar ?: "Tunai"
            tvOrderTotal.text = "Total: ${formatRupiah(order.totalHarga ?: 0)}"

            btnPrint.setOnClickListener { onPrintClick(order) }
            itemView.setOnClickListener { onPrintClick(order) }
        }
    }
}
