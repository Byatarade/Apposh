package com.byatara.penjualandev

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
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
    }

    private fun setupBottomNavigation() {
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.exit -> {
                    showExitDialog()
                    false // Return false so the item isn't selected visually as "active"
                }
                else -> true
            }
        }
    }

    private fun showExitDialog() {
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setCancelable(false)
        dialog.setContentView(R.layout.activity_exit)
        
        // Make the background transparent so the CardView corners show properly
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val btnCancel = dialog.findViewById<Button>(R.id.btnCancel)
        val btnConfirm = dialog.findViewById<Button>(R.id.btnConfirm)

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            dialog.dismiss()
            finishAffinity() // Closes the application
        }

        dialog.show()
    }
}