package com.byatara.penjualandev.adapter

import android.content.Context
import android.graphics.Color
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelProduk
import com.google.android.material.card.MaterialCardView
import com.google.android.material.chip.Chip
import java.text.NumberFormat
import java.util.Locale

class ProdukAdapter(
    private val produkList: MutableList<ModelProduk>
) : RecyclerView.Adapter<ProdukAdapter.ProdukViewHolder>() {

    private lateinit var appContext: Context

    // Simpan list penuh untuk keperluan filter
    private val fullList = mutableListOf<ModelProduk>()

    // Map untuk lookup nama dari ID
    private var mapCabang = mapOf<String, String>()
    private var mapKategori = mapOf<String, String>()

    private var isTransactionMode = false
    private var cartMap = mapOf<String, Int>()

    // Listener klik item
    interface OnItemClickListener {
        fun onItemClicked(produk: ModelProduk)
        fun onPlusClicked(produk: ModelProduk) {}
        fun onMinusClicked(produk: ModelProduk) {}
    }

    private var listener: OnItemClickListener? = null

    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    /**
     * Update map untuk translasi ID ke Nama.
     */
    fun updateMaps(cabang: Map<String, String>?, kategori: Map<String, String>?) {
        cabang?.let { this.mapCabang = it }
        kategori?.let { this.mapKategori = it }
        notifyDataSetChanged()
    }

    fun setTransactionMode(isTransaction: Boolean) {
        this.isTransactionMode = isTransaction
        notifyDataSetChanged()
    }

    fun updateCartMap(newCartMap: Map<String, Int>) {
        this.cartMap = newCartMap
        notifyDataSetChanged()
    }

    // ---------------------------------------------------------------
    // ViewHolder — memetakan semua view dari item_data_produk.xml
    // ---------------------------------------------------------------
    inner class ProdukViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        // Card container
        val cardProduk: MaterialCardView = itemView.findViewById(R.id.card_produk)

        // Gambar produk
        val imgProduk: ImageView = itemView.findViewById(R.id.img_produk)

        // Nama & harga
        val tvNamaProduk: TextView = itemView.findViewById(R.id.tv_nama_produk)
        val tvHargaProduk: TextView = itemView.findViewById(R.id.tv_harga_produk)

        // Chip status (Aktif / Nonaktif)
        val chipStatus: Chip = itemView.findViewById(R.id.chip_status)

        // Info dekoratif:
        //   ll_info_1 → Cabang
        //   ll_info_2 → Kategori
        //   ll_info_3 → Stok
        val llInfo1: LinearLayout = itemView.findViewById(R.id.ll_info_1)
        val llInfo2: LinearLayout = itemView.findViewById(R.id.ll_info_2)
        val llInfo3: LinearLayout = itemView.findViewById(R.id.ll_info_3)

        // TextView di dalam setiap LinearLayout (index 1: setelah ImageView)
        val tvCabang: TextView = llInfo1.getChildAt(1) as TextView
        val tvKategori: TextView = llInfo2.getChildAt(1) as TextView
        val tvStok: TextView = llInfo3.getChildAt(1) as TextView

        // Cart Controls
        val llCartControls: LinearLayout = itemView.findViewById(R.id.ll_cart_controls)
        val btnMinus: TextView = itemView.findViewById(R.id.btn_minus)
        val btnPlus: TextView = itemView.findViewById(R.id.btn_plus)
        val tvCartQty: TextView = itemView.findViewById(R.id.tv_cart_qty)

        /**
         * Ikat data ModelProduk ke semua komponen layout.
         */
        fun bind(produk: ModelProduk) {

            // ── Nama produk ──────────────────────────────────────────
            tvNamaProduk.text = produk.namaProduk ?: "-"

            // ── Harga jual (format Rupiah) ───────────────────────────
            val harga = produk.hargaJual ?: 0
            tvHargaProduk.text = formatRupiah(harga)

            // ── Gambar produk (Glide) ────────────────────────────────
            if (!produk.fotoProduk.isNullOrEmpty()) {
                Glide.with(appContext)
                    .load(produk.fotoProduk)
                    .placeholder(R.drawable.menu)
                    .error(R.drawable.menu)
                    .centerCrop()
                    .into(imgProduk)
            } else {
                imgProduk.setImageResource(R.drawable.menu)
            }

            // ── Info Cabang (ll_info_1) ──────────────────────────────
            val idCabang = produk.idCabang ?: ""
            val namaCabang = mapCabang[idCabang] ?: idCabang.ifEmpty { "Semua Cabang" }
            tvCabang.text = namaCabang

            // ── Info Kategori (ll_info_2) ────────────────────────────
            val idKategori = produk.idKategori ?: ""
            val namaKategori = mapKategori[idKategori] ?: idKategori.ifEmpty { "-" }
            tvKategori.text = namaKategori

            // ── Info Stok (ll_info_3) ────────────────────────────────
            val stok = produk.stokProduk ?: 0
            tvStok.text = if (produk.tanpaBatas == "ya") "Tak Terbatas" else "$stok pcs"

            // ── Chip Status ──────────────────────────────────────────
            val isAktif = produk.statusProduk?.lowercase() == "aktif"
            if (isAktif) {
                chipStatus.text = "Aktif"
                chipStatus.setTextColor(Color.parseColor("#4CAF50"))
                chipStatus.chipStrokeColor =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#4CAF50"))
                chipStatus.setChipIconResource(R.drawable.tick)
            } else {
                chipStatus.text = "Nonaktif"
                chipStatus.setTextColor(Color.parseColor("#F44336"))
                chipStatus.chipStrokeColor =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F44336"))
                chipStatus.setChipIconResource(R.drawable.tick)
            }

            // ── Cart Controls / Klik item ────────────────────────────
            if (isTransactionMode) {
                llCartControls.visibility = View.VISIBLE
                val qty = cartMap[produk.idProduk] ?: 0
                tvCartQty.text = qty.toString()

                btnMinus.setOnClickListener {
                    listener?.onMinusClicked(produk)
                }
                btnPlus.setOnClickListener {
                    listener?.onPlusClicked(produk)
                }
            } else {
                llCartControls.visibility = View.GONE
            }

            itemView.setOnClickListener {
                listener?.onItemClicked(produk)
            }
        }
    }

    // ---------------------------------------------------------------
    // RecyclerView.Adapter overrides
    // ---------------------------------------------------------------

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProdukViewHolder {
        appContext = parent.context
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_data_produk, parent, false)
        return ProdukViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProdukViewHolder, position: Int) {
        holder.bind(produkList[position])
    }

    override fun getItemCount(): Int = produkList.size

    // ---------------------------------------------------------------
    // Fungsi update & filter data
    // ---------------------------------------------------------------

    /**
     * Perbarui seluruh list (dipanggil ketika data Firebase berubah).
     * Menyimpan salinan lengkap untuk keperluan filter.
     */
    fun updateFullList(newList: List<ModelProduk>) {
        fullList.clear()
        fullList.addAll(newList)
        produkList.clear()
        produkList.addAll(newList)
        notifyDataSetChanged()
    }

    /**
     * Filter berdasarkan nama produk (case-insensitive).
     * Jika query kosong, tampilkan semua produk.
     */
    fun filter(query: String) {
        produkList.clear()
        if (query.isEmpty()) {
            produkList.addAll(fullList)
        } else {
            val lowerQuery = query.lowercase(Locale.getDefault())
            for (produk in fullList) {
                if (produk.namaProduk?.lowercase(Locale.getDefault())
                        ?.contains(lowerQuery) == true
                ) {
                    produkList.add(produk)
                }
            }
        }
        notifyDataSetChanged()
    }

    // ---------------------------------------------------------------
    // Helper
    // ---------------------------------------------------------------

    /** Format angka ke format Rupiah: Rp 10.000 */
    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }
}