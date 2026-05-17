package com.byatara.penjualandev

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
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
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class TransaksiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var chipGroup: ChipGroup
    private lateinit var searchView: androidx.appcompat.widget.SearchView

    private lateinit var adapter: ProdukAdapter
    private lateinit var produkViewModel: ProdukViewModel

    // Map idKategori -> namaKategori, used for chip filtering
    private val idKategoriMap = mutableMapOf<String, String>() // chipText -> idKategori

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
    }

    private fun setupRecyclerView() {
        adapter = ProdukAdapter(mutableListOf())
        adapter.setOnItemClickListener(object : ProdukAdapter.OnItemClickListener {
            override fun onItemClicked(produk: ModelProduk) {
                // TODO: tambah ke keranjang
            }
        })
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                produkViewModel.filter(query.orEmpty())
                return true
            }
            override fun onQueryTextChange(newText: String?): Boolean {
                produkViewModel.filter(newText.orEmpty())
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
            adapter.updateFullList(listProduk)
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

    private fun styleChip(chip: Chip) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val backgroundColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.colorSecondary),
            android.graphics.Color.TRANSPARENT
        )
        chip.chipBackgroundColor = android.content.res.ColorStateList(states, backgroundColors)
        val textColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.colorOnSecondary),
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
}
