package com.byatara.penjualandev.pegawai

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
import com.byatara.penjualandev.adapter.PegawaiAdapter
import com.byatara.penjualandev.model.ModelPegawai
import com.byatara.penjualandev.viewmodel.PegawaiViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataPegawaiActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var adapter: PegawaiAdapter
    private lateinit var pegawaiViewModel: PegawaiViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_pegawai)
        
        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        pegawaiViewModel = ViewModelProvider(this).get(PegawaiViewModel::class.java)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupSearchView()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvDATA_PEGAWAI)
        fabTambah = findViewById(R.id.fabDATA_PEGAWAI_Tambah)
        btnBack = findViewById(R.id.btn_back)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        adapter = PegawaiAdapter(mutableListOf())
        
        adapter.setOnShowClickListener { pegawai ->
            startActivity(Intent(this, DetailPegawaiActivity::class.java).apply {
                putExtra("PEGAWAI_DATA", pegawai)
            })
        }

        adapter.setOnEditClickListener { pegawai ->
            startActivity(Intent(this, ModPegawaiActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("PEGAWAI_DATA", pegawai)
            })
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                pegawaiViewModel.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                pegawaiViewModel.filter(newText.orEmpty())
                return true
            }
        })
    }

    private fun observeViewModel() {
        pegawaiViewModel.pegawaiList.observe(this, Observer { listPegawai ->
            adapter.updateFullList(listPegawai)
        })
    }

    private fun setupListeners() {
        fabTambah.setOnClickListener {
            startActivity(Intent(this, ModPegawaiActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
