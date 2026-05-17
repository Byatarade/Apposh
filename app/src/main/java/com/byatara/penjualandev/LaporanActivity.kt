package com.byatara.penjualandev

import android.os.Bundle
import android.view.View
import android.widget.AutoCompleteTextView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.ProdukAdapter
import com.byatara.penjualandev.model.ModelProduk
import com.byatara.penjualandev.viewmodel.ProdukViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.text.NumberFormat
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvTotalProduk: TextView
    private lateinit var tvTotalNilai: TextView

    private lateinit var adapter: ProdukAdapter
    private lateinit var produkViewModel: ProdukViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

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
        setupDateDropdown()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_laporan_produk)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)
        tvTotalProduk = findViewById(R.id.tv_total_produk)
        tvTotalNilai = findViewById(R.id.tv_total_nilai)
    }

    private fun setupRecyclerView() {
        adapter = ProdukAdapter(mutableListOf())
        adapter.setOnItemClickListener(object : ProdukAdapter.OnItemClickListener {
            override fun onItemClicked(produk: ModelProduk) {
                // Detail produk (opsional)
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupDateDropdown() {
        val spinnerTanggal = findViewById<AutoCompleteTextView>(R.id.spinner_tanggal) ?: return
        val dateRanges = arrayOf(
            "Hari Ini",
            "Kemarin",
            "Minggu Ini",
            "Bulan Ini",
            "Kustom Rentang Waktu..."
        )
        val dropdownAdapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            dateRanges
        )
        spinnerTanggal.setAdapter(dropdownAdapter)
    }

    private fun observeViewModel() {
        produkViewModel.produkList.observe(this, Observer { listProduk ->
            adapter.updateFullList(listProduk)

            // Hitung statistik dari data real
            val totalProduk = listProduk.size
            val totalNilaiStok = listProduk.sumOf { produk ->
                val hargaBeli = produk.hargaBeli ?: 0
                val stok = if (produk.tanpaBatas == "ya") 0 else (produk.stokProduk ?: 0)
                hargaBeli * stok
            }

            tvTotalProduk.text = totalProduk.toString()
            tvTotalNilai.text = formatRupiah(totalNilaiStok)
        })

        produkViewModel.cabangMap.observe(this, Observer { map ->
            adapter.updateMaps(cabang = map, kategori = null)
        })

        produkViewModel.kategoriMap.observe(this, Observer { map ->
            adapter.updateMaps(cabang = null, kategori = map)
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

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }
}
