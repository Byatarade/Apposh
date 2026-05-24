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
import com.byatara.penjualandev.model.ModelPelanggan
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView

class PelangganAdapter(
    private var pelangganList: List<ModelPelanggan>,
    private val isPickerMode: Boolean = false
) : RecyclerView.Adapter<PelangganAdapter.PelangganViewHolder>() {

    private var onShowClickListener: ((ModelPelanggan) -> Unit)? = null
    private var onEditClickListener: ((ModelPelanggan) -> Unit)? = null

    fun setOnShowClickListener(listener: (ModelPelanggan) -> Unit) {
        onShowClickListener = listener
    }

    fun setOnEditClickListener(listener: (ModelPelanggan) -> Unit) {
        onEditClickListener = listener
    }

    /** @deprecated Use setOnShowClickListener */
    fun setOnItemClickListener(listener: (ModelPelanggan) -> Unit) {
        onShowClickListener = listener
    }

    fun updateFullList(newList: List<ModelPelanggan>) {
        pelangganList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_pelanggan, parent, false)
        return PelangganViewHolder(view)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        holder.bind(pelangganList[position])
    }

    override fun getItemCount(): Int = pelangganList.size

    inner class PelangganViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_pelanggan_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_pelanggan_phone)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_pelanggan_address)
        private val btnHubungi: MaterialButton = itemView.findViewById(R.id.btn_hubungi)
        private val btnLihat: MaterialButton = itemView.findViewById(R.id.btn_lihat)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit)
        private val cardView: MaterialCardView = itemView.findViewById(R.id.card_pelanggan)

        fun bind(pelanggan: ModelPelanggan) {
            tvName.text = pelanggan.namaPelanggan ?: "-"
            tvPhone.text = pelanggan.teleponPelanggan ?: "-"
            tvAddress.text = pelanggan.alamatPelanggan ?: "-"

            if (isPickerMode) {
                btnHubungi.visibility = View.GONE
                btnLihat.visibility = View.GONE
                btnEdit.visibility = View.GONE
                val params = cardView.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, 4, 0, 4)
                cardView.layoutParams = params
                itemView.setOnClickListener { onShowClickListener?.invoke(pelanggan) }
                return
            }

            btnHubungi.visibility = View.VISIBLE
            btnLihat.visibility = View.VISIBLE
            btnEdit.visibility = View.VISIBLE

            btnHubungi.setOnClickListener {
                val phone = pelanggan.teleponPelanggan
                if (!phone.isNullOrEmpty()) {
                    val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    itemView.context.startActivity(intent)
                } else {
                    Toast.makeText(itemView.context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT)
                        .show()
                }
            }

            btnLihat.setOnClickListener {
                onShowClickListener?.invoke(pelanggan)
            }

            btnEdit.setOnClickListener {
                onEditClickListener?.invoke(pelanggan)
            }

            itemView.setOnClickListener {
                onShowClickListener?.invoke(pelanggan)
            }
        }
    }
}
