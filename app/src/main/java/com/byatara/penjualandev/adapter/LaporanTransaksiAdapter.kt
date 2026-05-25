package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelOrder
import java.text.NumberFormat
import java.util.Locale

class LaporanTransaksiAdapter(
    private var orderList: List<ModelOrder>
) : RecyclerView.Adapter<LaporanTransaksiAdapter.TransaksiViewHolder>() {

    fun updateData(newList: List<ModelOrder>) {
        orderList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransaksiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_laporan_transaksi, parent, false)
        return TransaksiViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransaksiViewHolder, position: Int) {
        holder.bind(orderList[position])
    }

    override fun getItemCount(): Int = orderList.size

    class TransaksiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvOrderId: TextView = itemView.findViewById(R.id.tv_order_id)
        private val tvOrderDate: TextView = itemView.findViewById(R.id.tv_order_date)
        private val tvOrderStaff: TextView = itemView.findViewById(R.id.tv_order_staff)
        private val tvOrderMethod: TextView = itemView.findViewById(R.id.tv_order_method)
        private val tvOrderTotal: TextView = itemView.findViewById(R.id.tv_order_total)
        private val tvOrderKeuntungan: TextView = itemView.findViewById(R.id.tv_order_keuntungan)

        fun bind(order: ModelOrder) {
            tvOrderId.text = order.idOrder ?: "-"
            tvOrderDate.text = order.tanggalWaktu ?: "-"
            
            val kasir = order.namaKasir ?: "Kasir Utama"
            val pelanggan = if (order.namaPelanggan.isNullOrEmpty()) "Pelanggan Umum" else order.namaPelanggan
            tvOrderStaff.text = "Kasir: $kasir | Pelanggan: $pelanggan"
            
            tvOrderMethod.text = order.metodeBayar ?: "Tunai"
            tvOrderTotal.text = "Total: " + formatRupiah(order.totalHarga ?: 0)
            
            val keuntungan = order.keuntungan ?: 0
            tvOrderKeuntungan.text = "Profit: " + (if (keuntungan >= 0) "+" else "") + formatRupiah(keuntungan)
            if (keuntungan >= 0) {
                tvOrderKeuntungan.setTextColor(android.graphics.Color.parseColor("#059669")) // Green
            } else {
                tvOrderKeuntungan.setTextColor(android.graphics.Color.parseColor("#EF4444")) // Red
            }
        }
    }
}
