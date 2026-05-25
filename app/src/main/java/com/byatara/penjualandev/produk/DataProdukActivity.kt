package com.byatara.penjualandev.produk

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.adapter.ProdukAdapter
import com.byatara.penjualandev.model.ModelProduk
import com.byatara.penjualandev.viewmodel.ProdukViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataProdukActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var viewLoading: ProgressBar
    
    private lateinit var adapter: ProdukAdapter
    private lateinit var produkViewModel: ProdukViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_produk)
        
        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Inisiasi ViewModel yang sudah kita buat sebelumnya
        produkViewModel = ViewModelProvider(this).get(ProdukViewModel::class.java)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupSearchView()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_data_produk)
        fabTambah = findViewById(R.id.fab_tambah_produk)
        btnBack = findViewById(R.id.btn_back)
        searchView = findViewById(R.id.searchView)
        viewLoading = findViewById(R.id.view_loading)
    }

    private fun setupRecyclerView() {
        adapter = ProdukAdapter(mutableListOf())
        
        adapter.setOnItemClickListener(object : ProdukAdapter.OnItemClickListener {
            override fun onItemClicked(produk: ModelProduk) {
                val intent = Intent(this@DataProdukActivity, DetailProdukActivity::class.java)
                intent.putExtra("PRODUK_DATA", produk)
                startActivity(intent)
            }
        })

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
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
            } else {
                viewLoading.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        })
    }

    private fun setupListeners() {
        // Arahkan ke TambahProdukActivity
        fabTambah.setOnClickListener {
            startActivity(Intent(this, TambahProdukActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
