package com.byatara.penjualandev

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Paksa aplikasi menggunakan Mode Terang (Light Mode) secara global
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Setup Bottom Navigation
        setupBottomNavigation()

        // Navigate to DataKategoriActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardkategori).setOnClickListener {
            startActivity(android.content.Intent(this, com.byatara.penjualandev.kategori.DataKategoriActivity::class.java))
        }

        // Navigate to DataProdukActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardmenu).setOnClickListener {
            startActivity(android.content.Intent(this, com.byatara.penjualandev.produk.DataProdukActivity::class.java))
        }

        // Navigate to DataCabangActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardcabang).setOnClickListener {
            startActivity(android.content.Intent(this, com.byatara.penjualandev.cabang.DataCabangActivity::class.java))
        }

        // Navigate to DataPegawaiActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardpegawai).setOnClickListener {
            startActivity(android.content.Intent(this, com.byatara.penjualandev.pegawai.DataPegawaiActivity::class.java))
        }

        // Navigate to TransaksiActivity (Mockup)
        findViewById<android.widget.LinearLayout>(R.id.btn_transaksi).setOnClickListener {
            startActivity(android.content.Intent(this, com.byatara.penjualandev.TransaksiActivity::class.java))
        }

        // Navigate to LaporanActivity (Mockup)
        findViewById<android.widget.LinearLayout>(R.id.btn_laporan).setOnClickListener {
            startActivity(android.content.Intent(this, com.byatara.penjualandev.LaporanActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        com.byatara.penjualandev.utils.BottomNavigationHelper.setup(this, R.id.navigation_home)
    }
}