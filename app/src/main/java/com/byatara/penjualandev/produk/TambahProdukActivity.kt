package com.byatara.penjualandev.produk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelProduk
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase
import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.CabangAdapter
import com.byatara.penjualandev.util.applyCleanSearchStyle
import com.byatara.penjualandev.adapter.KategoriAdapter
import com.byatara.penjualandev.viewmodel.CabangViewModel
import com.byatara.penjualandev.viewmodel.DataKategoriViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TambahProdukActivity : AppCompatActivity() {

    // Views
    private lateinit var btnBack: ImageView
    private lateinit var etNamaProduk: TextInputEditText
    private lateinit var etSku: TextInputEditText
    private lateinit var etBarcode: TextInputEditText
    private lateinit var btnPilihKategori: MaterialButton
    private lateinit var btnPilihCabang: MaterialButton
    
    private lateinit var etHargaBeli: TextInputEditText
    private lateinit var spinnerTipeKeuntungan: AutoCompleteTextView
    private lateinit var etNilaiProfit: TextInputEditText
    private lateinit var etHargaJual: TextInputEditText
    
    private lateinit var etStok: TextInputEditText
    private lateinit var cbStokTakTerbatas: MaterialCheckBox
    
    private lateinit var btnSimpan: MaterialButton

    private var selectedIdCabang: String = ""
    private var selectedIdKategori: String = ""
    private lateinit var cabangViewModel: CabangViewModel
    private lateinit var kategoriViewModel: DataKategoriViewModel

    // Firebase
    private val database = FirebaseDatabase.getInstance().getReference("produk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_produk)

        cabangViewModel = ViewModelProvider(this).get(CabangViewModel::class.java)
        kategoriViewModel = ViewModelProvider(this).get(DataKategoriViewModel::class.java)

        initViews()
        setupListeners()
        setupSpinner()
    }

    private fun initViews() {
        // App Bar Setup
        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)
        toolbar.setNavigationOnClickListener { finish() }

        // Form Dasar
        etNamaProduk = findViewById(R.id.et_nama_produk)
        etSku = findViewById(R.id.et_sku)
        etBarcode = findViewById(R.id.et_barcode)

        // Pilihan
        btnPilihKategori = findViewById(R.id.btn_pilih_kategori)
        btnPilihCabang = findViewById(R.id.btn_pilih_cabang)

        // Harga & Keuntungan
        etHargaBeli = findViewById(R.id.et_harga_beli)
        spinnerTipeKeuntungan = findViewById(R.id.spinner_tipe_keuntungan)
        etNilaiProfit = findViewById(R.id.et_nilai_profit)
        etHargaJual = findViewById(R.id.et_harga_jual)
        
        // Disable manual input on Harga Jual because it's calculated automatically
        etHargaJual.isEnabled = false

        // Manajemen Stok
        etStok = findViewById(R.id.et_stok)
        cbStokTakTerbatas = findViewById(R.id.cb_stok_tanpa_batas)

        btnSimpan = findViewById(R.id.btn_simpan)
        
        // Placeholder for Kamera/Galeri functionality (to be implemented later)
        findViewById<MaterialButton>(R.id.btn_kamera).setOnClickListener {
            Toast.makeText(this, "Fitur Kamera belum tersedia", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.btn_galeri).setOnClickListener {
            Toast.makeText(this, "Fitur Galeri belum tersedia", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupSpinner() {
        val tipeKeuntungan = arrayOf("Persentase (%)", "Nominal (Rp)")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, tipeKeuntungan)
        spinnerTipeKeuntungan.setAdapter(adapter)

        spinnerTipeKeuntungan.setOnItemClickListener { _, _, _, _ ->
            calculateHargaJual()
        }
    }

    private fun setupListeners() {
        val textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                calculateHargaJual()
            }
            override fun afterTextChanged(s: Editable?) {}
        }

        etHargaBeli.addTextChangedListener(textWatcher)
        etNilaiProfit.addTextChangedListener(textWatcher)

        // Logika checkbox stok tak terbatas
        cbStokTakTerbatas.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                etStok.setText("")
                etStok.isEnabled = false
            } else {
                etStok.isEnabled = true
            }
        }

        // Simpan Data
        btnSimpan.setOnClickListener { validsiDataProduk() }
        
        btnPilihKategori.setOnClickListener {
            showBottomSheetKategori()
        }
        
        btnPilihCabang.setOnClickListener {
            showBottomSheetCabang()
        }
    }

    private fun showBottomSheetKategori() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_kategori, null)
        bottomSheetDialog.setContentView(view)

        (view.parent as View).setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val searchView = view.findViewById<SearchView>(R.id.search_view_kategori)
        searchView.applyCleanSearchStyle()
        val rvKategori = view.findViewById<RecyclerView>(R.id.rv_kategori)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_kategori)

        val adapter = KategoriAdapter(mutableListOf())
        rvKategori.layoutManager = LinearLayoutManager(this)
        rvKategori.adapter = adapter

        adapter.setOnItemClickListener { kategori ->
            selectedIdKategori = kategori.idKategori ?: ""
            btnPilihKategori.text = kategori.namaKategori
            bottomSheetDialog.dismiss()
        }

        kategoriViewModel.kategoriList.observe(this, Observer { listKategori ->
            adapter.updateFullList(listKategori)
        })

        kategoriViewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                kategoriViewModel.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                kategoriViewModel.filter(newText.orEmpty())
                return true
            }
        })

        bottomSheetDialog.show()
    }

    private fun showBottomSheetCabang() {
        val bottomSheetDialog = BottomSheetDialog(this)
        val view = layoutInflater.inflate(R.layout.layout_bottom_sheet_cabang, null)
        bottomSheetDialog.setContentView(view)

        // Make background transparent to show custom rounded corners
        (view.parent as View).setBackgroundColor(android.graphics.Color.TRANSPARENT)

        val searchView = view.findViewById<SearchView>(R.id.search_view_cabang)
        searchView.applyCleanSearchStyle()
        val rvCabang = view.findViewById<RecyclerView>(R.id.rv_cabang)
        val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar_cabang)

        val adapter = CabangAdapter(mutableListOf())
        rvCabang.layoutManager = LinearLayoutManager(this)
        rvCabang.adapter = adapter

        adapter.setOnItemClickListener { cabang ->
            selectedIdCabang = cabang.idCabang ?: ""
            btnPilihCabang.text = cabang.namaCabang
            bottomSheetDialog.dismiss()
        }

        cabangViewModel.cabangList.observe(this, Observer { listCabang ->
            adapter.updateFullList(listCabang)
        })

        cabangViewModel.isLoading.observe(this, Observer { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        })

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                cabangViewModel.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                cabangViewModel.filter(newText.orEmpty())
                return true
            }
        })

        bottomSheetDialog.show()
    }

    private fun calculateHargaJual() {
        val hargaBeliStr = etHargaBeli.text.toString()
        val nilaiProfitStr = etNilaiProfit.text.toString()
        val tipeKeuntungan = spinnerTipeKeuntungan.text.toString()

        val hargaBeli = hargaBeliStr.toDoubleOrNull() ?: 0.0
        val profit = nilaiProfitStr.toDoubleOrNull() ?: 0.0
        var hargaJual = 0.0

        if (tipeKeuntungan == "Persentase (%)") {
            if (hargaBeli > 0) {
                hargaJual = hargaBeli + (hargaBeli * (profit / 100))
            }
        } else {
            // Nominal (Rp)
            hargaJual = hargaBeli + profit
        }

        if (hargaJual > 0) {
            etHargaJual.setText(hargaJual.toLong().toString())
        } else {
            etHargaJual.setText("0")
        }
    }

    private fun validsiDataProduk() {
        val namaProduk = etNamaProduk.text.toString().trim()
        val sku = etSku.text.toString().trim()
        val barcode = etBarcode.text.toString().trim()
        
        val hargaBeliStr = etHargaBeli.text.toString().trim()
        val hargaJualStr = etHargaJual.text.toString().trim()
        
        val tipeKeuntungan = spinnerTipeKeuntungan.text.toString().trim()
        val stokStr = etStok.text.toString().trim()
        val isTanpaBatas = cbStokTakTerbatas.isChecked

        if (namaProduk.isEmpty()) {
            etNamaProduk.error = "Nama Produk wajib diisi"
            etNamaProduk.requestFocus()
            return
        }

        if (hargaBeliStr.isEmpty()) {
            etHargaBeli.error = "Harga Beli wajib diisi"
            etHargaBeli.requestFocus()
            return
        }
        
        if (!isTanpaBatas && stokStr.isEmpty()) {
            etStok.error = "Stok wajib diisi jika tidak tak terbatas"
            etStok.requestFocus()
            return
        }

        val hargaBeli = hargaBeliStr.toIntOrNull() ?: 0
        val hargaJual = hargaJualStr.toIntOrNull() ?: 0
        val stok = if (isTanpaBatas) 0 else (stokStr.toIntOrNull() ?: 0)
        val stringTanpaBatas = if (isTanpaBatas) "ya" else "tidak"

        // Gabungkan SKU & Barcode untuk field deskripsi produk (atau simpan sesuai keperluan)
        val deskripsi = "SKU: $sku \nBarcode: $barcode"

        simpanKeFirebase(
            namaProduk = namaProduk,
            deskripsi = deskripsi,
            hargaBeli = hargaBeli,
            hargaJual = hargaJual,
            tipeKeuntungan = tipeKeuntungan,
            stok = stok,
            tanpaBatas = stringTanpaBatas,
            idCabang = selectedIdCabang,
            idKategori = selectedIdKategori
        )
    }

    private fun simpanKeFirebase(
        namaProduk: String,
        deskripsi: String,
        hargaBeli: Int,
        hargaJual: Int,
        tipeKeuntungan: String,
        stok: Int,
        tanpaBatas: String,
        idCabang: String,
        idKategori: String
    ) {
        val idProduk = database.push().key ?: return

        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val modelProduk = ModelProduk(
            idProduk = idProduk,
            namaProduk = namaProduk,
            fotoProduk = "", // Kosong sementara, nanti diisi link gambar
            deskripsiProduk = deskripsi,
            idKategori = idKategori,
            idCabang = idCabang,
            stokProduk = stok,
            tanpaBatas = tanpaBatas,
            hargaBeli = hargaBeli,
            hargaJual = hargaJual,
            tipeKeuntungan = tipeKeuntungan,
            manajemenStok = "aktif",
            statusProduk = "aktif",
            createdAt = currentDate,
            updatedAt = currentDate
        )

        // Panggil Toast Loading... (Opsional bisa pakai ProgressDialog)
        Toast.makeText(this, "Menyimpan produk...", Toast.LENGTH_SHORT).show()

        database.child(idProduk).setValue(modelProduk)
            .addOnSuccessListener {
                // Catat log histori aktivitas ke Firebase
                com.byatara.penjualandev.utils.CatatanHistori.catat(
                    judul = "Produk Ditambahkan",
                    deskripsi = "Produk baru '$namaProduk' berhasil didaftarkan ke sistem",
                    tipe = "produk"
                )

                Toast.makeText(this, "Produk berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish() // Kembali ke activity sebelumnya
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_LONG).show()
            }
    }
}
