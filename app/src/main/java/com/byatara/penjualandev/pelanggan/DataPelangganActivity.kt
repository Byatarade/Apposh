package com.byatara.penjualandev.pelanggan

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
import com.byatara.penjualandev.adapter.PelangganAdapter
import com.byatara.penjualandev.model.ModelPelanggan
import com.byatara.penjualandev.viewmodel.PelangganViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataPelangganActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var adapter: PelangganAdapter
    private lateinit var pelangganViewModel: PelangganViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pelanggan)

        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        pelangganViewModel = ViewModelProvider(this).get(PelangganViewModel::class.java)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupSearchView()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvDATA_PELANGGAN)
        fabTambah = findViewById(R.id.fabDATA_PELANGGAN_Tambah)
        btnBack = findViewById(R.id.btn_back)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        adapter = PelangganAdapter(mutableListOf())

        adapter.setOnShowClickListener { pelanggan ->
            startActivity(Intent(this, DetailPelangganActivity::class.java).apply {
                putExtra("PELANGGAN_DATA", pelanggan)
            })
        }

        adapter.setOnEditClickListener { pelanggan ->
            startActivity(Intent(this, ModPelangganActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("PELANGGAN_DATA", pelanggan)
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                pelangganViewModel.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pelangganViewModel.filter(newText.orEmpty())
                return true
            }
        })
    }

    private fun observeViewModel() {
        pelangganViewModel.pelangganList.observe(this, Observer { listPelanggan ->
            adapter.updateFullList(listPelanggan)
        })
    }

    private fun setupListeners() {
        fabTambah.setOnClickListener {
            startActivity(Intent(this, ModPelangganActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
