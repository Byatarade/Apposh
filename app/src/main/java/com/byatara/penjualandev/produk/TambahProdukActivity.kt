package com.byatara.penjualandev.produk

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelProduk
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.FirebaseDatabase

import android.view.View
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import android.net.Uri
import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.bumptech.glide.Glide
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
import java.text.NumberFormat
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
    
    private lateinit var etHargaJual: TextInputEditText
    
    private lateinit var etStok: TextInputEditText
    private lateinit var cbStokTakTerbatas: MaterialCheckBox
    private lateinit var switchStatusProduk: MaterialSwitch
    
    private lateinit var btnSimpan: MaterialButton


    private var selectedIdCabang: String = ""
    private var selectedIdKategori: String = ""
    private lateinit var cabangViewModel: CabangViewModel
    private lateinit var kategoriViewModel: DataKategoriViewModel

    // Image Upload (Base64)
    private var base64Image: String? = null
    
    private val imagePickerLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        if (uri != null) {
            val imgPreview = findViewById<ImageView>(R.id.img_preview)
            imgPreview.layoutParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            imgPreview.layoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
            imgPreview.scaleType = ImageView.ScaleType.CENTER_CROP
            imgPreview.imageTintList = null
            
            val tvPhotoHint = (imgPreview.parent as? android.widget.LinearLayout)?.getChildAt(1) as? TextView
            tvPhotoHint?.visibility = View.GONE

            Glide.with(this).load(uri).into(imgPreview)
            
            // Proses gambar ke Base64 (compress to small size)
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(uri)
                val originalBitmap = BitmapFactory.decodeStream(inputStream)
                
                // Resize bitmap to max 400px width/height to save database space
                val maxSize = 400
                val width = originalBitmap.width
                val height = originalBitmap.height
                val ratioBitmap = width.toFloat() / height.toFloat()
                
                var finalWidth = maxSize
                var finalHeight = maxSize
                if (ratioBitmap > 1) {
                    finalWidth = maxSize
                    finalHeight = (maxSize / ratioBitmap).toInt()
                } else {
                    finalHeight = maxSize
                    finalWidth = (maxSize * ratioBitmap).toInt()
                }
                
                val scaledBitmap = Bitmap.createScaledBitmap(originalBitmap, finalWidth, finalHeight, true)
                val outputStream = ByteArrayOutputStream()
                scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 60, outputStream)
                val byteArray = outputStream.toByteArray()
                
                // Tambahkan prefix agar mudah dikenali nantinya
                base64Image = "base64:" + Base64.encodeToString(byteArray, Base64.DEFAULT)
            } catch (e: Exception) {
                Toast.makeText(this, "Gagal memproses gambar: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Edit Mode
    private var isEditMode: Boolean = false
    private var existingProduk: ModelProduk? = null

    // Firebase
    private val database = FirebaseDatabase.getInstance().getReference("produk")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_tambah_produk)

        isEditMode = intent.getBooleanExtra("EDIT_MODE", false)
        existingProduk = intent.getParcelableExtra<ModelProduk>("PRODUK_DATA")

        cabangViewModel = ViewModelProvider(this).get(CabangViewModel::class.java)
        kategoriViewModel = ViewModelProvider(this).get(DataKategoriViewModel::class.java)

        initViews()
        setupListeners()

        if (isEditMode && existingProduk != null) {
            setupEditMode()
        }
    }

    private fun setupEditMode() {
        existingProduk?.let { p ->
            findViewById<TextView>(R.id.tv_toolbar_title).text = "Edit Produk"
            etNamaProduk.setText(p.namaProduk)
            
            // Format existing harga jual
            val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
            etHargaJual.setText(formatter.format(p.hargaJual ?: 0))
            switchStatusProduk.isChecked = p.statusProduk?.lowercase() == "aktif"
            
            // Parse deskripsi back to SKU & Barcode if possible
            val desc = p.deskripsiProduk ?: ""
            if (desc.contains("SKU: ") && desc.contains("Barcode: ")) {
                val lines = desc.split("\n")
                etSku.setText(lines[0].replace("SKU: ", "").trim())
                etBarcode.setText(lines[1].replace("Barcode: ", "").trim())
            }

            selectedIdKategori = p.idKategori ?: ""
            selectedIdCabang = p.idCabang ?: ""
            
            if (selectedIdKategori.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("kategori").child(selectedIdKategori).get()
                    .addOnSuccessListener { snapshot ->
                        btnPilihKategori.text = snapshot.child("namaKategori").value.toString()
                    }
            }

            if (selectedIdCabang.isNotEmpty()) {
                FirebaseDatabase.getInstance().getReference("cabang").child(selectedIdCabang).get()
                    .addOnSuccessListener { snapshot ->
                        btnPilihCabang.text = snapshot.child("namaCabang").value.toString()
                    }
            }

            // Removed spinnerTipeKeuntungan
            
            if (p.tanpaBatas == "ya") {
                cbStokTakTerbatas.isChecked = true
                etStok.setText("")
                etStok.isEnabled = false
            } else {
                cbStokTakTerbatas.isChecked = false
                etStok.setText(p.stokProduk.toString())
                etStok.isEnabled = true
            }

            btnSimpan.text = "Update Produk"
            
            // Load existing image if available
            if (!p.fotoProduk.isNullOrEmpty()) {
                val imgPreview = findViewById<ImageView>(R.id.img_preview)
                imgPreview.layoutParams.width = android.view.ViewGroup.LayoutParams.MATCH_PARENT
                imgPreview.layoutParams.height = android.view.ViewGroup.LayoutParams.MATCH_PARENT
                imgPreview.scaleType = ImageView.ScaleType.CENTER_CROP
                imgPreview.imageTintList = null
                
                val tvPhotoHint = (imgPreview.parent as? android.widget.LinearLayout)?.getChildAt(1) as? TextView
                tvPhotoHint?.visibility = View.GONE

                val foto = p.fotoProduk
                if (foto != null && foto.startsWith("base64:")) {
                    val base64Str = foto.substring(7)
                    val decodedString = Base64.decode(base64Str, Base64.DEFAULT)
                    Glide.with(this).load(decodedString).into(imgPreview)
                    base64Image = foto // simpan supaya tidak terhapus jika tidak ganti foto
                } else {
                    Glide.with(this).load(foto).into(imgPreview)
                    base64Image = foto
                }
            }
        }
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

        etHargaJual = findViewById(R.id.et_harga_jual)
        etHargaJual.isEnabled = true

        // Manajemen Stok
        etStok = findViewById(R.id.et_stok)
        cbStokTakTerbatas = findViewById(R.id.cb_stok_tanpa_batas)
        switchStatusProduk = findViewById(R.id.switch_status_produk)

        btnSimpan = findViewById(R.id.btn_simpan)

        if (!isEditMode) {
            switchStatusProduk.isChecked = true
        }
        
        // Placeholder for Kamera/Galeri functionality (to be implemented later)
        findViewById<MaterialButton>(R.id.btn_kamera).setOnClickListener {
            Toast.makeText(this, "Fitur Kamera belum tersedia", Toast.LENGTH_SHORT).show()
        }
        findViewById<MaterialButton>(R.id.btn_galeri).setOnClickListener {
            imagePickerLauncher.launch("image/*")
        }
    }

    // Removed setupSpinner

    private fun setupListeners() {
        etHargaJual.addTextChangedListener(object : TextWatcher {
            private var current = ""
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                if (s.toString() != current) {
                    etHargaJual.removeTextChangedListener(this)
                    
                    val cleanString = s.toString().replace("[^0-9]".toRegex(), "").trim()
                    if (cleanString.isNotEmpty()) {
                        val parsed = cleanString.toDoubleOrNull() ?: 0.0
                        val formatter = NumberFormat.getNumberInstance(Locale("id", "ID"))
                        val formatted = formatter.format(parsed)
                        current = formatted
                        etHargaJual.setText(formatted)
                        etHargaJual.setSelection(formatted.length)
                    } else {
                        current = ""
                        etHargaJual.setText("")
                    }
                    
                    etHargaJual.addTextChangedListener(this)
                }
            }
        })

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
            val activeKategori = listKategori.filter { it.statusKategori == true }
            adapter.updateFullList(activeKategori)
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
            val activeCabang = listCabang.filter { it.statusCabang == true }
            adapter.updateFullList(activeCabang)
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

    // Removed calculateHargaJual

    private fun validsiDataProduk() {
        val namaProduk = etNamaProduk.text.toString().trim()
        val sku = etSku.text.toString().trim()
        val barcode = etBarcode.text.toString().trim()
        
        val hargaJualStr = etHargaJual.text.toString().replace("[^0-9]".toRegex(), "").trim()
        
        val stokStr = etStok.text.toString().trim()
        val isTanpaBatas = cbStokTakTerbatas.isChecked

        if (namaProduk.isEmpty()) {
            etNamaProduk.error = "Nama Produk wajib diisi"
            etNamaProduk.requestFocus()
            return
        }

        if (hargaJualStr.isEmpty()) {
            etHargaJual.error = "Harga Jual wajib diisi"
            etHargaJual.requestFocus()
            return
        }
        
        if (!isTanpaBatas && stokStr.isEmpty()) {
            etStok.error = "Stok wajib diisi jika tidak tak terbatas"
            etStok.requestFocus()
            return
        }

        val hargaJual = hargaJualStr.toIntOrNull() ?: 0
        val stok = if (isTanpaBatas) 0 else (stokStr.toIntOrNull() ?: 0)
        val stringTanpaBatas = if (isTanpaBatas) "ya" else "tidak"

        // Gabungkan SKU & Barcode untuk field deskripsi produk (atau simpan sesuai keperluan)
        val deskripsi = "SKU: $sku \nBarcode: $barcode"

        simpanKeFirebase(
            namaProduk = namaProduk,
            deskripsi = deskripsi,
            hargaBeli = 0,
            hargaJual = hargaJual,
            tipeKeuntungan = "",
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
        val idProduk = if (isEditMode && existingProduk != null) {
            existingProduk?.idProduk ?: ""
        } else {
            database.push().key ?: return
        }

        val statusProduk = if (switchStatusProduk.isChecked) "aktif" else "nonaktif"
        val currentDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date())

        val modelProduk = ModelProduk(
            idProduk = idProduk,
            namaProduk = namaProduk,
            fotoProduk = existingProduk?.fotoProduk ?: "",
            deskripsiProduk = deskripsi,
            idKategori = idKategori,
            idCabang = idCabang,
            stokProduk = stok,
            tanpaBatas = tanpaBatas,
            hargaBeli = hargaBeli,
            hargaJual = hargaJual,
            tipeKeuntungan = tipeKeuntungan,
            manajemenStok = existingProduk?.manajemenStok ?: "aktif",
            statusProduk = statusProduk,
            createdAt = existingProduk?.createdAt ?: currentDate,
            updatedAt = currentDate
        )

        val loadingMsg = if (isEditMode) "Mengupdate produk..." else "Menyimpan produk..."
        Toast.makeText(this, loadingMsg, Toast.LENGTH_SHORT).show()
        
        // Disable save button to prevent double clicks
        btnSimpan.isEnabled = false

        if (base64Image != null) {
            modelProduk.fotoProduk = base64Image
        }
        
        saveDataToDatabase(idProduk, modelProduk, namaProduk)
    }
    
    private fun saveDataToDatabase(idProduk: String, modelProduk: ModelProduk, namaProduk: String) {

        database.child(idProduk).setValue(modelProduk).addOnCompleteListener { task ->
            btnSimpan.isEnabled = true
            if (task.isSuccessful) {
                if (!isEditMode) {
                    com.byatara.penjualandev.utils.CatatanHistori.catat(
                        judul = "Produk Ditambahkan",
                        deskripsi = "Produk baru '$namaProduk' berhasil didaftarkan ke sistem",
                        tipe = "produk"
                    )
                }
                val msg = if (isEditMode) "Produk berhasil diupdate" else "Produk berhasil ditambahkan"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            } else {
                Toast.makeText(this, "Gagal menyimpan data: ${task.exception?.message}", Toast.LENGTH_LONG).show()
            }
        }
    }
}
