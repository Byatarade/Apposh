package com.byatara.penjualandev

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import android.content.Intent
import com.google.android.material.button.MaterialButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.ProdukPosAdapter
import com.byatara.penjualandev.model.ModelProduk
import com.byatara.penjualandev.model.ModelOrder
import com.byatara.penjualandev.model.ModelOrderItem
import com.byatara.penjualandev.model.ModelPelanggan
import com.byatara.penjualandev.model.ModelPegawai
import com.byatara.penjualandev.adapter.PelangganAdapter
import com.byatara.penjualandev.adapter.PegawaiAdapter
import com.byatara.penjualandev.pelanggan.ModPelangganActivity
import com.byatara.penjualandev.pegawai.ModPegawaiActivity
import com.byatara.penjualandev.util.applyCleanSearchStyle
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.viewmodel.PegawaiViewModel
import com.byatara.penjualandev.viewmodel.PelangganViewModel
import com.byatara.penjualandev.viewmodel.ProdukViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import androidx.recyclerview.widget.LinearLayoutManager
import java.util.Locale

class TransaksiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var searchView: androidx.appcompat.widget.SearchView

    private lateinit var adapter: ProdukPosAdapter
    private lateinit var produkViewModel: ProdukViewModel

    private lateinit var tvCartItems: TextView
    private lateinit var tvCartTotal: TextView
    private lateinit var btnPay: MaterialButton
    private lateinit var cardCartBar: View

    private val cartMap = mutableMapOf<String, Pair<ModelProduk, Int>>() // idProduk -> Pair(Produk, Quantity)

    // Map idKategori -> namaKategori, used for chip filtering
    private val idKategoriMap = mutableMapOf<String, String>() // chipText -> idKategori

    private var selectedPelanggan: ModelPelanggan? = null
    private var selectedPegawai: ModelPegawai? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

        // Setup window insets for edge-to-edge layout
        val mainView = findViewById<View>(android.R.id.content)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Handle back button on toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Init ViewModel
        produkViewModel = ViewModelProvider(this).get(ProdukViewModel::class.java)

        initViews()
        setupRecyclerView()
        setupSearchView()
        setupCategoryChips()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_produk_transaksi)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)
        chipGroup = findViewById(R.id.chip_group_kategori)
        searchView = findViewById(R.id.search_produk)

        tvCartItems = findViewById(R.id.tv_cart_items)
        tvCartTotal = findViewById(R.id.tv_cart_total)
        btnPay = findViewById(R.id.btn_pay)
        cardCartBar = findViewById(R.id.card_cart_bar)

        btnPay.setOnClickListener {
            showOrderDetailBottomSheet()
        }
    }

    private fun setupRecyclerView() {
        adapter = ProdukPosAdapter(mutableListOf())
        adapter.setOnItemClickListener(object : ProdukPosAdapter.OnItemClickListener {
            override fun onPlusClicked(produk: ModelProduk) {
                val idProduk = produk.idProduk ?: return
                val currentQty = cartMap[idProduk]?.second ?: 0
                
                // Stock validation
                val isUnlimited = produk.tanpaBatas == "ya"
                val maxStok = produk.stokProduk ?: 0
                if (!isUnlimited && currentQty >= maxStok) {
                    Toast.makeText(this@TransaksiActivity, "Stok produk tidak mencukupi!", Toast.LENGTH_SHORT).show()
                    return
                }

                cartMap[idProduk] = Pair(produk, currentQty + 1)
                updateCartUI()
            }

            override fun onMinusClicked(produk: ModelProduk) {
                val idProduk = produk.idProduk ?: return
                val currentQty = cartMap[idProduk]?.second ?: 0
                if (currentQty > 1) {
                    cartMap[idProduk] = Pair(produk, currentQty - 1)
                } else {
                    cartMap.remove(idProduk)
                }
                updateCartUI()
            }
        })
        recyclerView.layoutManager = GridLayoutManager(this, 2)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                adapter.filter(query.orEmpty())
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                adapter.filter(newText.orEmpty())
                return true
            }
        })
    }

    private fun setupCategoryChips() {
        val database = FirebaseDatabase.getInstance()
        val kategoriRef = database.getReference("kategori")

        kategoriRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    chipGroup.removeAllViews()
                    idKategoriMap.clear()

                    // Add "Semua" chip
                    val allChip = Chip(this@TransaksiActivity, null, com.google.android.material.R.attr.chipStyle).apply {
                        id = View.generateViewId()
                        text = "Semua"
                        isCheckable = true
                        isChecked = true
                    }
                    styleChip(allChip)
                    allChip.setOnClickListener {
                        produkViewModel.filterByKategori(null) // tampilkan semua
                    }
                    chipGroup.addView(allChip)

                    // Add kategori from Firebase (aktif only)
                    for (dataSnapshot in snapshot.children) {
                        val kategori = dataSnapshot.getValue(com.byatara.penjualandev.model.ModelKategori::class.java)
                        if (kategori != null && kategori.statusKategori == true) {
                            val namaKategori = kategori.namaKategori ?: continue
                            val idKategori = kategori.idKategori ?: dataSnapshot.key ?: continue

                            idKategoriMap[namaKategori] = idKategori

                            val chip = Chip(this@TransaksiActivity, null, com.google.android.material.R.attr.chipStyle).apply {
                                id = View.generateViewId()
                                text = namaKategori
                                isCheckable = true
                            }
                            styleChip(chip)
                            chip.setOnClickListener {
                                produkViewModel.filterByKategori(idKategori)
                            }
                            chipGroup.addView(chip)
                        }
                    }
                }
            }

            override fun onCancelled(error: DatabaseError) {}
        })
    }

    private fun observeViewModel() {
        produkViewModel.produkList.observe(this, Observer { listProduk ->
            // Hanya tampilkan produk dengan status "aktif" pada transaksi POS
            val activeProduk = listProduk.filter { it.statusProduk?.lowercase() == "aktif" }
            adapter.updateData(activeProduk)
        })

        produkViewModel.kategoriMap.observe(this, Observer { map ->
            adapter.updateKategoriMap(map ?: emptyMap())
        })

        produkViewModel.isLoading.observe(this, Observer { isLoading ->
            if (isLoading) {
                viewLoading.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                layoutEmpty.visibility = View.GONE
            } else {
                viewLoading.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })

        produkViewModel.isSearchEmpty.observe(this, Observer { isEmpty ->
            if (isEmpty) {
                layoutEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            } else {
                layoutEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })
    }

    private fun styleChip(chip: Chip) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val backgroundColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary),
            android.graphics.Color.TRANSPARENT
        )
        chip.chipBackgroundColor = android.content.res.ColorStateList(states, backgroundColors)
        val textColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.white),
            androidx.core.content.ContextCompat.getColor(this, R.color.colorSecondaryText)
        )
        chip.setTextColor(android.content.res.ColorStateList(states, textColors))
        val strokeColors = intArrayOf(
            android.graphics.Color.TRANSPARENT,
            androidx.core.content.ContextCompat.getColor(this, R.color.colorOutlineStroke)
        )
        chip.chipStrokeColor = android.content.res.ColorStateList(states, strokeColors)
        chip.chipStrokeWidth = resources.displayMetrics.density * 1f
    }

    private fun updateCartUI() {
        var totalItems = 0
        var totalPrice = 0
        for ((_, item) in cartMap) {
            val produk = item.first
            val qty = item.second
            totalItems += qty
            totalPrice += (produk.hargaJual ?: 0) * qty
        }

        if (totalItems > 0) {
            cardCartBar.visibility = View.VISIBLE
            tvCartItems.text = getString(R.string.items_format, totalItems)
            tvCartTotal.text = formatRupiah(totalPrice)
        } else {
            cardCartBar.visibility = View.GONE
            tvCartItems.text = getString(R.string.belum_ada_pesanan)
            tvCartTotal.text = "Rp 0"
        }

        // Update adapter to reflect cart qty
        val adapterCartMap = cartMap.mapValues { it.value.second }
        adapter.updateCart(adapterCartMap)
    }

    private fun showOrderDetailBottomSheet() {
        if (cartMap.isEmpty()) {
            Toast.makeText(this, "Keranjang masih kosong!", Toast.LENGTH_SHORT).show()
            return
        }

        val dialog = BottomSheetDialog(this, com.google.android.material.R.style.Theme_Design_BottomSheetDialog)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_order_detail, null)
        dialog.setContentView(view)

        val btnPilihPegawai = view.findViewById<MaterialButton>(R.id.btn_pilih_pegawai)
        val btnPilihPelanggan = view.findViewById<MaterialButton>(R.id.btn_pilih_pelanggan)
        val etCatatan = view.findViewById<TextInputEditText>(R.id.et_catatan)
        val btnProceed = view.findViewById<MaterialButton>(R.id.btn_proceed_payment)

        // Reset selections for new order
        selectedPelanggan = null
        selectedPegawai = null

        btnPilihPegawai.setOnClickListener {
            showPilihPegawaiDialog(btnPilihPegawai)
        }

        btnPilihPelanggan.setOnClickListener {
            showPilihPelangganDialog(btnPilihPelanggan)
        }

        btnProceed.setOnClickListener {
            val kasir = selectedPegawai?.namaPegawai ?: "Kasir Utama"
            val pelanggan = selectedPelanggan?.namaPelanggan ?: "Pelanggan Umum"
            val catatan = etCatatan.text.toString().trim()

            // Create Order data transfer
            var totalItems = 0
            var subtotalPrice = 0
            val orderItems = ArrayList<ModelOrderItem>()

            for ((_, item) in cartMap) {
                val p = item.first
                val qty = item.second
                val subtotal = (p.hargaJual ?: 0) * qty
                
                totalItems += qty
                subtotalPrice += subtotal

                orderItems.add(
                    ModelOrderItem(
                        idProduk = p.idProduk,
                        namaProduk = p.namaProduk,
                        fotoProduk = p.fotoProduk,
                        hargaJual = p.hargaJual,
                        hargaBeli = p.hargaBeli ?: 0,
                        qty = qty,
                        subtotal = subtotal,
                        tanpaBatas = p.tanpaBatas
                    )
                )
            }

            // Hitung Pajak (PPN 11%) & Total Akhir
            val pajak = (subtotalPrice * 0.11).toInt()
            val totalAkhir = subtotalPrice + pajak

            val formatter = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val formattedDate = formatter.format(java.util.Date())

            val orderData = ModelOrder(
                idOrder = "ORD-${System.currentTimeMillis() / 1000}",
                namaKasir = kasir,
                namaPelanggan = pelanggan,
                nomorMeja = "",
                catatan = catatan,
                items = orderItems,
                subtotal = subtotalPrice,
                pajak = pajak,
                totalHarga = totalAkhir,
                status = "PAID",
                timestamp = System.currentTimeMillis(),
                tanggalWaktu = formattedDate,
                idCabang = cartMap.values.firstOrNull()?.first?.idCabang // Pakai idCabang produk pertama
            )

            dialog.dismiss()

            // Navigate to PembayaranActivity
            val intent = Intent(this@TransaksiActivity, PembayaranActivity::class.java).apply {
                putExtra("ORDER_DATA", orderData)
            }
            startActivity(intent)
        }

        dialog.show()
    }

    private fun showPilihPelangganDialog(btnPilihPelanggan: MaterialButton) {
        val dialog = BottomSheetDialog(this, com.google.android.material.R.style.Theme_Design_BottomSheetDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_pilih_pelanggan, null)
        dialog.setContentView(dialogView)

        val searchView = dialogView.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_pelanggan_dialog)
        searchView.applyCleanSearchStyle()
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_pelanggan_dialog)
        val btnPelangganUmum = dialogView.findViewById<MaterialButton>(R.id.btn_pelanggan_umum)
        val btnTambahBaru = dialogView.findViewById<MaterialButton>(R.id.btn_tambah_pelanggan_baru)

        val pelangganViewModel = ViewModelProvider(this)[PelangganViewModel::class.java]
        val dialogAdapter = PelangganAdapter(emptyList(), isPickerMode = true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = dialogAdapter

        val pelangganObserver = Observer<List<ModelPelanggan>> { list ->
            val aktifSaja = list.filter { it.statusPelanggan != false }
            dialogAdapter.updateFullList(aktifSaja)
        }
        pelangganViewModel.pelangganList.observe(this, pelangganObserver)
        pelangganViewModel.filter("")

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                pelangganViewModel.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pelangganViewModel.filter(newText.orEmpty())
                return true
            }
        })

        dialogAdapter.setOnItemClickListener { pelanggan ->
            selectedPelanggan = pelanggan
            btnPilihPelanggan.text = "Pelanggan: ${pelanggan.namaPelanggan}"
            dialog.dismiss()
        }

        btnPelangganUmum.setOnClickListener {
            selectedPelanggan = null
            btnPilihPelanggan.text = "Pelanggan: Pelanggan Umum"
            dialog.dismiss()
        }

        btnTambahBaru.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, ModPelangganActivity::class.java))
        }

        dialog.setOnDismissListener {
            pelangganViewModel.pelangganList.removeObserver(pelangganObserver)
            pelangganViewModel.filter("")
        }

        dialog.show()
    }

    private fun showPilihPegawaiDialog(btnPilihPegawai: MaterialButton) {
        val dialog = BottomSheetDialog(this, com.google.android.material.R.style.Theme_Design_BottomSheetDialog)
        val dialogView = layoutInflater.inflate(R.layout.dialog_pilih_pegawai, null)
        dialog.setContentView(dialogView)

        val searchView = dialogView.findViewById<androidx.appcompat.widget.SearchView>(R.id.search_pegawai_dialog)
        searchView.applyCleanSearchStyle()
        val recyclerView = dialogView.findViewById<RecyclerView>(R.id.rv_pegawai_dialog)
        val btnKasirUtama = dialogView.findViewById<MaterialButton>(R.id.btn_kasir_utama)
        val btnTambahBaru = dialogView.findViewById<MaterialButton>(R.id.btn_tambah_pegawai_baru)

        val pegawaiViewModel = ViewModelProvider(this)[PegawaiViewModel::class.java]
        val dialogAdapter = PegawaiAdapter(emptyList(), isPickerMode = true)

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = dialogAdapter

        val pegawaiObserver = Observer<List<ModelPegawai>> { list ->
            val aktifSaja = list.filter { it.statusPegawai != false }
            dialogAdapter.updateFullList(aktifSaja)
        }
        pegawaiViewModel.pegawaiList.observe(this, pegawaiObserver)
        pegawaiViewModel.filter("")

        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                pegawaiViewModel.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pegawaiViewModel.filter(newText.orEmpty())
                return true
            }
        })

        dialogAdapter.setOnItemClickListener { pegawai ->
            selectedPegawai = pegawai
            btnPilihPegawai.text = "Kasir: ${pegawai.namaPegawai}"
            dialog.dismiss()
        }

        btnKasirUtama.setOnClickListener {
            selectedPegawai = null
            btnPilihPegawai.text = "Kasir: Kasir Utama"
            dialog.dismiss()
        }

        btnTambahBaru.setOnClickListener {
            dialog.dismiss()
            startActivity(Intent(this, ModPegawaiActivity::class.java))
        }

        dialog.setOnDismissListener {
            pegawaiViewModel.pegawaiList.removeObserver(pegawaiObserver)
            pegawaiViewModel.filter("")
        }

        dialog.show()
    }
}
