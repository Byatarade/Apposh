package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelCabang
import com.google.android.material.chip.Chip

class CabangAdapter(
    private var cabangList: List<ModelCabang>
) : RecyclerView.Adapter<CabangAdapter.CabangViewHolder>() {

    private var onItemClickListener: ((ModelCabang) -> Unit)? = null

    fun setOnItemClickListener(listener: (ModelCabang) -> Unit) {
        onItemClickListener = listener
    }

    fun updateFullList(newList: List<ModelCabang>) {
        cabangList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CabangViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_cabang, parent, false)
        return CabangViewHolder(view)
    }

    override fun onBindViewHolder(holder: CabangViewHolder, position: Int) {
        val cabang = cabangList[position]
        holder.bind(cabang)
    }

    override fun getItemCount(): Int = cabangList.size

    inner class CabangViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.cabang_name)
        private val tvAlamat: TextView = itemView.findViewById(R.id.cabang_alamat)
        private val chipStatus: Chip = itemView.findViewById(R.id.cabang_status)

        fun bind(cabang: ModelCabang) {
            tvName.text = cabang.namaCabang
            tvAlamat.text = cabang.alamatCabang

            if (cabang.statusCabang == true) {
                chipStatus.text = "Aktif"
            } else {
                chipStatus.text = "Non Aktif"
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(cabang)
            }
        }
    }
}
