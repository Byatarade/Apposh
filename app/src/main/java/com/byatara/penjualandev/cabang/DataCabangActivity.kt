package com.byatara.penjualandev.cabang

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
import com.byatara.penjualandev.adapter.CabangAdapter
import com.byatara.penjualandev.model.ModelCabang
import com.byatara.penjualandev.viewmodel.CabangViewModel
import com.google.android.material.floatingactionbutton.FloatingActionButton

class DataCabangActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var viewLoading: ProgressBar
    
    private lateinit var adapter: CabangAdapter
    private lateinit var cabangViewModel: CabangViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_cabang)
        
        val mainView = findViewById<View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        cabangViewModel = ViewModelProvider(this).get(CabangViewModel::class.java)

        initViews()
        setupRecyclerView()
        setupListeners()
        setupSearchView()
        observeViewModel()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvDATA_CABANG)
        fabTambah = findViewById(R.id.fab_tambah_cabang)
        btnBack = findViewById(R.id.btn_back)
        searchView = findViewById(R.id.searchView)
        viewLoading = findViewById(R.id.view_loading)
    }

    private fun setupRecyclerView() {
        adapter = CabangAdapter(mutableListOf())
        
        adapter.setOnItemClickListener { cabang ->
            val intent = Intent(this, ModCabangActivity::class.java)
            intent.putExtra("CABANG_DATA", cabang)
            intent.putExtra("IS_EDIT", true)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupSearchView() {
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
    }

    private fun observeViewModel() {
        cabangViewModel.cabangList.observe(this, Observer { listCabang ->
            adapter.updateFullList(listCabang)
        })

        cabangViewModel.isLoading.observe(this, Observer { isLoading ->
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
        fabTambah.setOnClickListener {
            startActivity(Intent(this, ModCabangActivity::class.java))
        }

        btnBack.setOnClickListener {
            finish()
        }
    }
}
