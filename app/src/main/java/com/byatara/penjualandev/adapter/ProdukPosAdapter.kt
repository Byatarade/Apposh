package com.byatara.penjualandev.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelProduk
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class ProdukPosAdapter(
    private val produkList: MutableList<ModelProduk>
) : RecyclerView.Adapter<ProdukPosAdapter.ProdukPosViewHolder>() {

    private lateinit var context: Context
    private val fullList = mutableListOf<ModelProduk>()
    private var cartMap = mapOf<String, Int>() // idProduk -> Qty
    
    private var mapKategori = mapOf<String, String>()

    interface OnItemClickListener {
        fun onPlusClicked(produk: ModelProduk)
        fun onMinusClicked(produk: ModelProduk)
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    fun updateData(newList: List<ModelProduk>) {
        fullList.clear()
        fullList.addAll(newList)
        produkList.clear()
        produkList.addAll(newList)
        notifyDataSetChanged()
    }

    fun updateCart(newCartMap: Map<String, Int>) {
        this.cartMap = newCartMap
        notifyDataSetChanged()
    }

    fun updateKategoriMap(kategori: Map<String, String>) {
        this.mapKategori = kategori
        notifyDataSetChanged()
    }

    fun filter(query: String) {
        produkList.clear()
        if (query.isEmpty()) {
            produkList.addAll(fullList)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            for (p in fullList) {
                if (p.namaProduk?.lowercase(Locale.getDefault())?.contains(lowerQuery) == true) {
                    produkList.add(p)
                }
            }
        }
        notifyDataSetChanged()
    }

    inner class ProdukPosViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imgProduk: ImageView = itemView.findViewById(R.id.img_produk)
        val chipKategori: Chip = itemView.findViewById(R.id.chip_kategori_badge)
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val tvHargaProduk: TextView = itemView.findViewById(R.id.tv_harga_produk)
        val tvStok: TextView = itemView.findViewById(R.id.tv_stok)
        
        val btnAddInitial: MaterialButton = itemView.findViewById(R.id.btn_add_initial)
        val llQtyControls: LinearLayout = itemView.findViewById(R.id.ll_qty_controls)
        val btnMinus: MaterialButton = itemView.findViewById(R.id.btn_minus_pos)
        val btnPlus: MaterialButton = itemView.findViewById(R.id.btn_plus_pos)
        val tvCartQty: TextView = itemView.findViewById(R.id.tv_cart_qty_pos)

        fun bind(produk: ModelProduk) {
            tvNamaProduk.text = produk.namaProduk ?: "-"
            tvHargaProduk.text = formatRupiah(produk.hargaJual ?: 0)

            // Category Badge
            val idKategori = produk.idKategori ?: ""
            val namaKategori = mapKategori[idKategori] ?: idKategori.ifEmpty { "Umum" }
            chipKategori.text = namaKategori

            // Glide Image
            if (!produk.fotoProduk.isNullOrEmpty()) {
                Glide.with(context)
                    .load(produk.fotoProduk)
                    .placeholder(R.drawable.menu)
                    .error(R.drawable.menu)
                    .centerCrop()
                    .into(imgProduk)
            } else {
                imgProduk.setImageResource(R.drawable.menu)
            }

            // Stock status
            val isUnlimited = produk.tanpaBatas == "ya"
            val currentStok = produk.stokProduk ?: 0
            val currentQtyInCart = cartMap[produk.idProduk] ?: 0
            val availableStok = if (isUnlimited) 9999 else (currentStok - currentQtyInCart)

            tvStok.text = if (isUnlimited) "Stok: Tak Terbatas" else "Stok: $currentStok"
            if (!isUnlimited && currentStok <= 0) {
                tvStok.text = "Stok Habis"
                tvStok.setTextColor(context.getColor(R.color.red))
            } else {
                tvStok.setTextColor(context.getColor(R.color.colorSecondaryText))
            }

            // Cart state logic
            if (currentQtyInCart > 0) {
                btnAddInitial.visibility = View.GONE
                llQtyControls.visibility = View.VISIBLE
                tvCartQty.text = currentQtyInCart.toString()

                // Disable plus button if out of stock
                if (!isUnlimited && availableStok <= 0) {
                    btnPlus.isEnabled = false
                    btnPlus.alpha = 0.5f
                } else {
                    btnPlus.isEnabled = true
                    btnPlus.alpha = 1.0f
                }

                btnMinus.setOnClickListener {
                    listener?.onMinusClicked(produk)
                }
                btnPlus.setOnClickListener {
                    listener?.onPlusClicked(produk)
                }
            } else {
                btnAddInitial.visibility = View.VISIBLE
                llQtyControls.visibility = View.GONE

                // Handle out of stock for initial addition
                if (!isUnlimited && currentStok <= 0) {
                    btnAddInitial.text = "Habis"
                    btnAddInitial.isEnabled = false
                    btnAddInitial.strokeColor = android.content.res.ColorStateList.valueOf(Color.LTGRAY)
                    btnAddInitial.setTextColor(Color.LTGRAY)
                } else {
                    btnAddInitial.text = "Tambah"
                    btnAddInitial.isEnabled = true
                    btnAddInitial.strokeColor = android.content.res.ColorStateList.valueOf(context.getColor(R.color.colorPrimary))
                    btnAddInitial.setTextColor(context.getColor(R.color.colorPrimary))
                }

                btnAddInitial.setOnClickListener {
                    listener?.onPlusClicked(produk)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukPosViewHolder {
        context = parent.context
        val view = LayoutInflater.from(context).inflate(R.layout.item_produk_pos, parent, false)
        return ProdukPosViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukPosViewHolder, position: Int) {
        holder.bind(produkList[position])
    }

    override fun getItemCount(): Int = produkList.size
}
