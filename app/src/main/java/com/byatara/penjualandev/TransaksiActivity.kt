package com.byatara.penjualandev

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class TransaksiActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaksi)

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

        // Load categories dynamically from Firebase
        setupCategoryChips()
    }

    private fun setupCategoryChips() {
        val chipGroup = findViewById<com.google.android.material.chip.ChipGroup>(R.id.chip_group_kategori) ?: return
        val database = com.google.firebase.database.FirebaseDatabase.getInstance()
        val kategoriRef = database.getReference("kategori")

        kategoriRef.addValueEventListener(object : com.google.firebase.database.ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                if (snapshot.exists()) {
                    chipGroup.removeAllViews()

                    // Add "Semua" Chip
                    val allChip = com.google.android.material.chip.Chip(
                        this@TransaksiActivity, 
                        null, 
                        com.google.android.material.R.attr.chipStyle
                    ).apply {
                        text = "Semua"
                        isCheckable = true
                        isChecked = true
                    }
                    chipGroup.addView(allChip)

                    // Add other categories from Database
                    for (dataSnapshot in snapshot.children) {
                        val kategori = dataSnapshot.getValue(com.byatara.penjualandev.model.ModelKategori::class.java)
                        if (kategori != null && kategori.statusKategori == true) {
                            val chip = com.google.android.material.chip.Chip(
                                this@TransaksiActivity, 
                                null, 
                                com.google.android.material.R.attr.chipStyle
                            ).apply {
                                text = kategori.namaKategori
                                isCheckable = true
                            }
                            chipGroup.addView(chip)
                        }
                    }
                }
            }

            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {
                // Log or handle database error
            }
        })
    }
    }
}
