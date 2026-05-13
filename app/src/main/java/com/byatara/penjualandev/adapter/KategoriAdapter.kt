package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelKategori
import com.google.android.material.chip.Chip

class KategoriAdapter(
    private var kategoriList: List<ModelKategori>
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    private var onItemClickListener: ((ModelKategori) -> Unit)? = null

    fun setOnItemClickListener(listener: (ModelKategori) -> Unit) {
        onItemClickListener = listener
    }

    fun updateFullList(newList: List<ModelKategori>) {
        kategoriList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_kategori, parent, false)
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        val kategori = kategoriList[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int = kategoriList.size

    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.kategori_name)
        private val chipStatus: Chip = itemView.findViewById(R.id.kategori_status)

        fun bind(kategori: ModelKategori) {
            tvName.text = kategori.namaKategori

            if (kategori.statusKategori == true) {
                chipStatus.text = "Aktif"
            } else {
                chipStatus.text = "Non Aktif"
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(kategori)
            }
        }
    }
}