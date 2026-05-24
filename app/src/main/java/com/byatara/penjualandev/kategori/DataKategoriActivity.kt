package com.byatara.penjualandev.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.byatara.penjualandev.adapter.KategoriAdapter
import com.byatara.penjualandev.model.ModelKategori
import com.byatara.penjualandev.viewmodel.DataKategoriViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var adapter: KategoriAdapter
    private lateinit var kategoriViewModel: DataKategoriViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)
        
        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        kategoriViewModel = ViewModelProvider(this).get(DataKategoriViewModel::class.java)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupSearchView()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvDATA_KATEGORI)
        fabTambah = findViewById(R.id.fabDATA_KATEGORI_Tambah)
        btnBack = findViewById(R.id.btn_back)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        adapter = KategoriAdapter(mutableListOf())
        
        adapter.setOnItemClickListener { kategori ->
            startActivity(Intent(this, DetailKategoriActivity::class.java).apply {
                putExtra("KATEGORI_DATA", kategori)
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
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
    }

    private fun observeViewModel() {
        kategoriViewModel.kategoriList.observe(this, Observer { listKategori ->
            adapter.updateFullList(listKategori)
        })
    }

    private fun setupListeners() {
        fabTambah.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
