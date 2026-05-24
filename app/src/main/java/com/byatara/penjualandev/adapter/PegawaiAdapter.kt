package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelPegawai
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import android.content.Intent
import android.net.Uri
class PegawaiAdapter(
    private var pegawaiList: List<ModelPegawai>,
    private val isPickerMode: Boolean = false
) : RecyclerView.Adapter<PegawaiAdapter.PegawaiViewHolder>() {

    private var onItemClickListener: ((ModelPegawai) -> Unit)? = null

    fun setOnItemClickListener(listener: (ModelPegawai) -> Unit) {
        onItemClickListener = listener
    }

    fun updateFullList(newList: List<ModelPegawai>) {
        pegawaiList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_data_pegawai, parent, false)
        return PegawaiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        val pegawai = pegawaiList[position]
        holder.bind(pegawai)
    }

    override fun getItemCount(): Int = pegawaiList.size

    inner class PegawaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_pegawai_name)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_pegawai_address)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_pegawai_phone)
        private val tvCabang: TextView = itemView.findViewById(R.id.tv_pegawai_cabang)
        private val tvJoined: TextView = itemView.findViewById(R.id.tv_pegawai_joined)
        
        private val btnHubungi: MaterialButton = itemView.findViewById(R.id.btn_hubungi)
        private val btnLihat: MaterialButton = itemView.findViewById(R.id.btn_lihat)

        fun bind(pegawai: ModelPegawai) {
            tvName.text = pegawai.namaPegawai ?: "-"
            tvAddress.text = pegawai.alamatPegawai ?: "-"
            tvPhone.text = pegawai.teleponPegawai ?: "-"
            tvCabang.text = pegawai.idCabang ?: "-"
            tvJoined.text = "Bergabung pada ${pegawai.tanggalBergabung ?: "-"}"

            btnHubungi.setOnClickListener {
                val phone = pegawai.teleponPegawai
                if (!phone.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(itemView.context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }

            btnLihat.setOnClickListener {
                onItemClickListener?.invoke(pegawai)
            }
            
            if (isPickerMode) {
                btnHubungi.visibility = View.GONE
                btnLihat.visibility = View.GONE
            } else {
                btnHubungi.visibility = View.VISIBLE
                btnLihat.visibility = View.VISIBLE
            }
            
            // Allow clicking the card itself to also view
            itemView.setOnClickListener {
                onItemClickListener?.invoke(pegawai)
            }
        }
    }
}
