package com.byatara.penjualandev.adapter

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelPegawai
import com.google.android.material.button.MaterialButton

class PegawaiAdapter(
    private var pegawaiList: List<ModelPegawai>,
    private val isPickerMode: Boolean = false
) : RecyclerView.Adapter<PegawaiAdapter.PegawaiViewHolder>() {

    private var onShowClickListener: ((ModelPegawai) -> Unit)? = null
    private var onEditClickListener: ((ModelPegawai) -> Unit)? = null

    fun setOnShowClickListener(listener: (ModelPegawai) -> Unit) {
        onShowClickListener = listener
    }

    fun setOnEditClickListener(listener: (ModelPegawai) -> Unit) {
        onEditClickListener = listener
    }

    /** @deprecated Use setOnShowClickListener */
    fun setOnItemClickListener(listener: (ModelPegawai) -> Unit) {
        onShowClickListener = listener
    }

    fun updateFullList(newList: List<ModelPegawai>) {
        pegawaiList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PegawaiViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_pegawai, parent, false)
        return PegawaiViewHolder(view)
    }

    override fun onBindViewHolder(holder: PegawaiViewHolder, position: Int) {
        holder.bind(pegawaiList[position])
    }

    override fun getItemCount(): Int = pegawaiList.size

    inner class PegawaiViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_pegawai_name)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_pegawai_address)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_pegawai_phone)
        private val tvCabang: TextView = itemView.findViewById(R.id.tv_pegawai_cabang)
        private val tvJoined: TextView = itemView.findViewById(R.id.tv_pegawai_joined)
        private val btnHubungi: MaterialButton = itemView.findViewById(R.id.btn_hubungi)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit)

        fun bind(pegawai: ModelPegawai) {
            tvName.text = pegawai.namaPegawai ?: "-"
            tvAddress.text = pegawai.alamatPegawai ?: "-"
            tvPhone.text = pegawai.teleponPegawai ?: "-"
            tvCabang.text = pegawai.idCabang ?: "-"
            tvJoined.text = "Bergabung pada ${pegawai.tanggalBergabung ?: "-"}"

            if (isPickerMode) {
                btnHubungi.visibility = View.GONE
                btnEdit.visibility = View.GONE
                itemView.setOnClickListener { onShowClickListener?.invoke(pegawai) }
                return
            }

            btnHubungi.visibility = View.VISIBLE
            btnEdit.visibility = View.VISIBLE

            btnHubungi.setOnClickListener {
                val phone = pegawai.teleponPegawai
                if (!phone.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(itemView.context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            btnEdit.setOnClickListener {
                onEditClickListener?.invoke(pegawai)
            }

            itemView.setOnClickListener {
                onShowClickListener?.invoke(pegawai)
            }
        }
    }
}
