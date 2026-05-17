package com.byatara.penjualandev

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LaporanActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

        // Setup window insets for edge-to-edge layout
        val mainView = findViewById<android.view.View>(android.R.id.content)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }
        
        // Handle back button on toolbar
        val toolbar = findViewById<com.google.android.material.appbar.MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Setup Date Range Dropdown items
        setupDateDropdown()
    }

    private fun setupDateDropdown() {
        val spinnerTanggal = findViewById<android.widget.AutoCompleteTextView>(R.id.spinner_tanggal) ?: return
        
        val dateRanges = arrayOf(
            "Hari Ini",
            "Kemarin",
            "Minggu Ini",
            "Bulan Ini (01 Nov - 30 Nov 2026)",
            "Kustom Rentang Waktu..."
        )

        val adapter = android.widget.ArrayAdapter(
            this,
            android.R.layout.simple_dropdown_item_1line,
            dateRanges
        )
        
        spinnerTanggal.setAdapter(adapter)
    }
    }
}
