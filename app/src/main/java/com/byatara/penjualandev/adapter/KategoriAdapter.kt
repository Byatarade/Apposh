package com.byatara.penjualandev.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.kategori.Kategori
import com.google.android.material.chip.Chip

class KategoriAdapter(
    private val kategoriList: MutableList<Kategori>,
    private val onItemClick: (Kategori) -> Unit
) : RecyclerView.Adapter<KategoriAdapter.KategoriViewHolder>() {

    // Full list from Firebase (used as the source of truth for filtering)
    private var fullList = mutableListOf<Kategori>()

    inner class KategoriViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNamaKategori: TextView = itemView.findViewById(R.id.kategori_name)
        val chipStatus: Chip = itemView.findViewById(R.id.kategori_status)

        fun bind(kategori: Kategori) {
            tvNamaKategori.text = kategori.name

            // Set status chip
            if (kategori.isActive) {
                chipStatus.text = "Aktif"
            } else {
                chipStatus.text = "Nonaktif"
            }

            itemView.setOnClickListener {
                onItemClick(kategori)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): KategoriViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_kategori, parent, false)
        return KategoriViewHolder(view)
    }

    override fun onBindViewHolder(holder: KategoriViewHolder, position: Int) {
        holder.bind(kategoriList[position])
    }

    override fun getItemCount(): Int = kategoriList.size

    /**
     * Update the full list (called when Firebase data changes).
     * This stores a copy of the complete list for filtering purposes.
     */
    fun updateFullList(newList: List<Kategori>) {
        fullList.clear()
        fullList.addAll(newList)
    }

    /**
     * Filter the displayed list based on the search query.
     * Filters by category name (case-insensitive).
     * If query is empty, shows all items from the full list.
     */
    fun filter(query: String) {
        kategoriList.clear()
        if (query.isEmpty()) {
            kategoriList.addAll(fullList)
        } else {
            val lowerQuery = query.lowercase()
            for (kategori in fullList) {
                if (kategori.name.lowercase().contains(lowerQuery)) {
                    kategoriList.add(kategori)
                }
            }
        }
        notifyDataSetChanged()
    }
}