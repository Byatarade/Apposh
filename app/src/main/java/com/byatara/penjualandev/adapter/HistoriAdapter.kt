package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelHistori

class HistoriAdapter(
    private val historiList: MutableList<ModelHistori>
) : RecyclerView.Adapter<HistoriAdapter.HistoriViewHolder>() {

    inner class HistoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvJudul: TextView = itemView.findViewById(R.id.tv_judul_histori)
        val tvDeskripsi: TextView = itemView.findViewById(R.id.tv_deskripsi_histori)
        val tvWaktu: TextView = itemView.findViewById(R.id.tv_waktu_histori)
        val imgIcon: ImageView = itemView.findViewById(R.id.img_icon_histori)

        fun bind(histori: ModelHistori) {
            tvJudul.text = histori.judul ?: "-"
            tvDeskripsi.text = histori.deskripsi ?: "-"
            tvWaktu.text = histori.tanggalWaktu ?: "-"

            // Pilih icon sesuai tipe aktivitas
            val iconRes = when (histori.tipe?.lowercase()) {
                "produk" -> R.drawable.menu
                "kategori" -> R.drawable.label
                "cabang" -> R.drawable.cabang
                "pegawai" -> R.drawable.pegawai
                "transaksi" -> R.drawable.transaksii
                else -> R.drawable.ic_analytics
            }
            imgIcon.setImageResource(iconRes)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_histori, parent, false)
        return HistoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: HistoriViewHolder, position: Int) {
        holder.bind(historiList[position])
    }

    override fun getItemCount(): Int = historiList.size

    fun updateData(newList: List<ModelHistori>) {
        historiList.clear()
        historiList.addAll(newList)
        notifyDataSetChanged()
    }
}
