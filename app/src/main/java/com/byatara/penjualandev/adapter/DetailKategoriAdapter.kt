package com.byatara.penjualandev.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.model.ModelKategori
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import com.byatara.penjualandev.R
import com.google.android.material.chip.Chip

class DetailKategoriAdapter (private val kategorilist: List<ModelKategori>) :
    RecyclerView.Adapter<DetailKategoriAdapter.KategoriViewHolder>(){
    lateinit var appContext: Context
    interface OnItemClickListener {
        fun onItemClicked(kategori: ModelKategori)
    }
    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DetailKategoriAdapter.KategoriViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(
            R.layout.item_data_kategori, parent, false)
        appContext = parent.context
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: DetailKategoriAdapter.KategoriViewHolder,
        position: Int
    ) {
        val kategori = kategorilist[position]
        holder.bind(kategori)
    }

    override fun getItemCount(): Int {
        return kategorilist.size
    }
    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
     val tvNamaKategori: TextView = itemView.findViewById(R.id.kategori_name)
     val chipstatus: Chip = itemView.findViewById(R.id.kategori_status)
        fun bind(kategori: ModelKategori) {
            tvNamaKategori.text = kategori.namaKategori
            itemView.setOnClickListener {
                listener?.onItemClicked(kategori)
            }
        }
    }
}