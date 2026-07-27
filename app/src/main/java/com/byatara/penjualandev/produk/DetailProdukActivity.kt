package com.byatara.penjualandev.produk

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.util.Base64
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelProduk
import com.byatara.penjualandev.util.formatRupiah
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

class DetailProdukActivity : AppCompatActivity() {

    private lateinit var ivFoto: ImageView
    private lateinit var tvNama: TextView
    private lateinit var tvHarga: TextView
    private lateinit var chipStatus: Chip
    private lateinit var tvCabang: TextView
    private lateinit var tvKategori: TextView
    private lateinit var tvStok: TextView
    private lateinit var tvHargaBeli: TextView
    private lateinit var btnEdit: MaterialButton

    private var produk: ModelProduk? = null
    private val database = FirebaseDatabase.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_produk)

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        produk = intent.getParcelableExtra("PRODUK_DATA")

        initViews()
        displayProdukData()
        loadMaps()
    }

    private fun initViews() {
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        ivFoto = findViewById(R.id.iv_detail_foto)
        tvNama = findViewById(R.id.tv_detail_nama)
        tvHarga = findViewById(R.id.tv_detail_harga)
        chipStatus = findViewById(R.id.chip_detail_status)
        tvCabang = findViewById(R.id.tv_detail_cabang)
        tvKategori = findViewById(R.id.tv_detail_kategori)
        tvStok = findViewById(R.id.tv_detail_stok)
        tvHargaBeli = findViewById(R.id.tv_detail_harga_beli)
        btnEdit = findViewById(R.id.btn_edit_produk)
        val btnHapus = findViewById<MaterialButton>(R.id.btn_hapus_produk)

        btnEdit.setOnClickListener {
            val intent = Intent(this, TambahProdukActivity::class.java)
            intent.putExtra("EDIT_MODE", true)
            intent.putExtra("PRODUK_DATA", produk)
            startActivity(intent)
        }

        btnHapus.setOnClickListener {
            produk?.let { p ->
                val nama = p.namaProduk ?: "Produk ini"
                MaterialAlertDialogBuilder(this)
                    .setTitle("Hapus Produk")
                    .setMessage("Apakah Anda yakin ingin menghapus \"$nama\"?")
                    .setNegativeButton("Batal", null)
                    .setPositiveButton("Hapus") { _, _ ->
                        deleteProduk(p)
                    }
                    .show()
            }
        }
    }

    private fun deleteProduk(p: ModelProduk) {
        val id = p.idProduk ?: return
        database.getReference("produk").child(id).removeValue().addOnSuccessListener {
            // Catat log histori aktivitas ke Firebase
            com.byatara.penjualandev.utils.CatatanHistori.catat(
                judul = "Produk Dihapus",
                deskripsi = "Menghapus produk '${p.namaProduk}'",
                tipe = "produk"
            )
            Toast.makeText(this, "Produk berhasil dihapus", Toast.LENGTH_SHORT).show()
            finish()
        }.addOnFailureListener {
            Toast.makeText(this, "Gagal menghapus produk: ${it.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun displayProdukData() {
        produk?.let { p ->
            tvNama.text = p.namaProduk ?: "-"
            tvHarga.text = formatRupiah(p.hargaJual ?: 0)
            tvHargaBeli.text = formatRupiah(p.hargaBeli ?: 0)
            
            val stok = p.stokProduk ?: 0
            tvStok.text = if (p.tanpaBatas == "ya") "Tak Terbatas" else "$stok pcs"

            val isAktif = p.statusProduk?.lowercase() == "aktif"
            if (isAktif) {
                chipStatus.text = "Aktif"
                chipStatus.setChipBackgroundColorResource(R.color.green_success)
            } else {
                chipStatus.text = "Nonaktif"
                chipStatus.setChipBackgroundColorResource(R.color.colorError)
            }

            if (!p.fotoProduk.isNullOrEmpty()) {
                val foto = p.fotoProduk
                if (foto != null && foto.startsWith("base64:")) {
                    val base64Str = foto.substring(7)
                    val decodedString = Base64.decode(base64Str, Base64.DEFAULT)
                    Glide.with(this)
                        .load(decodedString)
                        .placeholder(R.drawable.menu)
                        .error(R.drawable.menu)
                        .centerCrop()
                        .into(ivFoto)
                } else {
                    Glide.with(this)
                        .load(foto)
                        .placeholder(R.drawable.menu)
                        .error(R.drawable.menu)
                        .centerCrop()
                        .into(ivFoto)
                }
                ivFoto.setPadding(0, 0, 0, 0)
                ivFoto.imageTintList = null
            }

            tvCabang.text = p.idCabang ?: "Semua Cabang"
            tvKategori.text = p.idKategori ?: "-"
        }
    }

    private fun loadMaps() {
        // Load Cabang Name
        produk?.idCabang?.let { id ->
            if (id.isNotEmpty()) {
                database.getReference("cabang").child(id).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        tvCabang.text = snapshot.child("namaCabang").value.toString()
                    }
                }
            }
        }

        // Load Kategori Name
        produk?.idKategori?.let { id ->
            if (id.isNotEmpty()) {
                database.getReference("kategori").child(id).get().addOnSuccessListener { snapshot ->
                    if (snapshot.exists()) {
                        tvKategori.text = snapshot.child("namaKategori").value.toString()
                    }
                }
            }
        }
    }
}
