package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.cabang.DataCabangActivity
import com.byatara.penjualandev.kategori.DataKategoriActivity
import com.byatara.penjualandev.pegawai.DataPegawaiActivity
import com.byatara.penjualandev.produk.DataProdukActivity
import com.byatara.penjualandev.pelanggan.DataPelangganActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val database = FirebaseDatabase.getInstance()
    private val usersRef = database.getReference("users")

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

        auth = FirebaseAuth.getInstance()
        val tvGreeting = findViewById<TextView>(R.id.tvGreeting)

        // Ambil data user yang sedang login
        val currentUser = auth.currentUser
        if (currentUser != null) {
            usersRef.child(currentUser.uid).get().addOnSuccessListener { snapshot ->
                if (snapshot.exists()) {
                    val name = snapshot.child("name").value.toString()
                    tvGreeting.text = name
                } else {
                    tvGreeting.text = currentUser.email?.split("@")?.get(0) ?: "User"
                }
            }.addOnFailureListener {
                tvGreeting.text = currentUser.email?.split("@")?.get(0) ?: "User"
            }
        }

        // Setup Bottom Navigation
        setupBottomNavigation()

        // Navigate to DataKategoriActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardkategori).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }

        // Navigate to DataProdukActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardmenu).setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }

        // Navigate to DataCabangActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardcabang).setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }

        // Navigate to DataPegawaiActivity
        findViewById<androidx.cardview.widget.CardView>(R.id.cardpegawai).setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        // Navigate to TransaksiActivity (Mockup)
        findViewById<android.widget.LinearLayout>(R.id.btn_transaksi).setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }

        // Navigate to DataPelangganActivity
        findViewById<android.widget.LinearLayout>(R.id.btn_pelanggan).setOnClickListener {
            startActivity(Intent(this, DataPelangganActivity::class.java))
        }

        // Navigate to LaporanActivity (Mockup)
        findViewById<android.widget.LinearLayout>(R.id.btn_laporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        com.byatara.penjualandev.utils.BottomNavigationHelper.setup(this, R.id.navigation_home)
    }
}