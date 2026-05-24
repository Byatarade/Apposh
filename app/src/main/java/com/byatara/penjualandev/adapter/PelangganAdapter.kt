package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelPelanggan
import com.google.android.material.button.MaterialButton
import android.widget.Toast
import android.content.Intent
import android.net.Uri

class PelangganAdapter(
    private var pelangganList: List<ModelPelanggan>,
    private val isPickerMode: Boolean = false
) : RecyclerView.Adapter<PelangganAdapter.PelangganViewHolder>() {

    private var onItemClickListener: ((ModelPelanggan) -> Unit)? = null

    fun setOnItemClickListener(listener: (ModelPelanggan) -> Unit) {
        onItemClickListener = listener
    }

    fun updateFullList(newList: List<ModelPelanggan>) {
        pelangganList = newList
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PelangganViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_pelanggan, parent, false)
        return PelangganViewHolder(view)
    }

    override fun onBindViewHolder(holder: PelangganViewHolder, position: Int) {
        val pelanggan = pelangganList[position]
        holder.bind(pelanggan)
    }

    override fun getItemCount(): Int = pelangganList.size

    inner class PelangganViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(R.id.tv_pelanggan_name)
        private val tvPhone: TextView = itemView.findViewById(R.id.tv_pelanggan_phone)
        private val tvAddress: TextView = itemView.findViewById(R.id.tv_pelanggan_address)
        
        private val btnHubungi: MaterialButton = itemView.findViewById(R.id.btn_hubungi)
        private val btnEdit: MaterialButton = itemView.findViewById(R.id.btn_edit)
        private val cardView: com.google.android.material.card.MaterialCardView = itemView.findViewById(R.id.card_pelanggan)

        fun bind(pelanggan: ModelPelanggan) {
            tvName.text = pelanggan.namaPelanggan ?: "-"
            tvPhone.text = pelanggan.teleponPelanggan ?: "-"
            tvAddress.text = pelanggan.alamatPelanggan ?: "-"

            if (isPickerMode) {
                // Hide action buttons in picker mode
                btnHubungi.visibility = View.GONE
                btnEdit.visibility = View.GONE
                // Adjust card padding/margins for compact dialog list
                val params = cardView.layoutParams as ViewGroup.MarginLayoutParams
                params.setMargins(0, 4, 0, 4)
                cardView.layoutParams = params
            } else {
                btnHubungi.visibility = View.VISIBLE
                btnEdit.visibility = View.VISIBLE

                btnHubungi.setOnClickListener {
                    val phone = pelanggan.teleponPelanggan
                    if (!phone.isNullOrEmpty()) {
                        val intent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                        itemView.context.startActivity(intent)
                    } else {
                        Toast.makeText(itemView.context, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
                    }
                }

                btnEdit.setOnClickListener {
                    onItemClickListener?.invoke(pelanggan)
                }
            }

            itemView.setOnClickListener {
                onItemClickListener?.invoke(pelanggan)
            }
        }
    }
}
