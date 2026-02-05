package com.byatara.penjualandev.kategori

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.R
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var ettambahkategori: TextView
    private lateinit var nama_kategori_layout: TextInputLayout
    private lateinit var nama_kategori_edit_text: TextInputEditText
    private lateinit var statuskategori: TextInputLayout
    private lateinit var status_kategori_auto_complete: AutoCompleteTextView
    private lateinit var button_simpan: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)
        
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            
            // Get original padding from XML (in pixels)
            val originalPaddingLeft = v.paddingLeft
            val originalPaddingTop = v.paddingTop
            val originalPaddingRight = v.paddingRight
            val originalPaddingBottom = v.paddingBottom
            
            // Add system bars insets to original padding instead of replacing it
            v.setPadding(
                originalPaddingLeft + systemBars.left,
                originalPaddingTop + systemBars.top,
                originalPaddingRight + systemBars.right,
                originalPaddingBottom + systemBars.bottom
            )
            insets
        }

        initViews()
        setupDropdown()
        setupListeners()
    }

    private fun initViews() {
        ettambahkategori = findViewById(R.id.ettambahkategori)
        nama_kategori_layout = findViewById(R.id.nama_kategori_layout)
        nama_kategori_edit_text = findViewById(R.id.nama_kategori_edit_text)
        statuskategori = findViewById(R.id.statuskategori)
        status_kategori_auto_complete = findViewById(R.id.status_kategori_auto_complete)
        button_simpan = findViewById(R.id.button_simpan)
    }

    private fun setupDropdown() {
        // Setup dropdown items for status kategori
        val statusItems = arrayOf("Aktif", "Non Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusItems)
        status_kategori_auto_complete.setAdapter(adapter)
        
        // Set default value
        status_kategori_auto_complete.setText("Aktif", false)
    }

    private fun setupListeners() {
        // Setup button simpan click listener
        button_simpan.setOnClickListener {
            saveKategori()
        }
    }

    private fun saveKategori() {
        val namaKategori = nama_kategori_edit_text.text.toString().trim()
        val statusKategori = status_kategori_auto_complete.text.toString().trim()

        // Validation
        if (namaKategori.isEmpty()) {
            nama_kategori_layout.error = "Nama kategori tidak boleh kosong"
            return
        } else {
            nama_kategori_layout.error = null
        }

        if (statusKategori.isEmpty()) {
            statuskategori.error = "Status kategori harus dipilih"
            return
        } else {
            statuskategori.error = null
        }

        // Create kategori object
        val kategoriId = myRef.push().key ?: return
        val isActive = statusKategori == "Aktif"
        
        val kategoriData = hashMapOf(
            "id" to kategoriId,
            "name" to namaKategori,
            "isActive" to isActive
        )

        // Save to Firebase
        myRef.child(kategoriId).setValue(kategoriData)
            .addOnSuccessListener {
                Toast.makeText(this, "Kategori berhasil ditambahkan", Toast.LENGTH_SHORT).show()
                finish() // Close activity and return to DataKategoriActivity
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal menambahkan kategori: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
