package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelKategori
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.database.FirebaseDatabase

class KategoriAdapter(
    private var kategoriList: List<ModelKategori>
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    private val database = FirebaseDatabase.getInstance().getReference("kategori")


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
        private val switchStatus: MaterialSwitch = itemView.findViewById(R.id.kategori_switch_status)

        fun bind(kategori: ModelKategori) {
            tvName.text = kategori.namaKategori

            // Set switch state without triggering listener
            switchStatus.setOnCheckedChangeListener(null)
            switchStatus.isChecked = kategori.statusKategori == true

            switchStatus.setOnCheckedChangeListener { _, isChecked ->
                kategori.idKategori?.let { id ->
                    database.child(id).child("statusKategori").setValue(isChecked)
                }
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(kategori)
            }
        }
    }
}