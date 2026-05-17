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

import com.byatara.penjualandev.model.ModelKategori

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var ettambahkategori: TextView
    private lateinit var nama_kategori_layout: TextInputLayout
    private lateinit var nama_kategori_edit_text: TextInputEditText
    private lateinit var statuskategori: TextInputLayout
    private lateinit var status_kategori_auto_complete: AutoCompleteTextView
    private lateinit var button_simpan: Button

    private var isEdit = false
    private var existingKategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_kategori)
        
        val mainView = findViewById<androidx.constraintlayout.widget.ConstraintLayout>(R.id.main)
        if (mainView != null) {
            val initialPaddingLeft = mainView.paddingLeft
            val initialPaddingTop = mainView.paddingTop
            val initialPaddingRight = mainView.paddingRight
            val initialPaddingBottom = mainView.paddingBottom

            ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(
                    initialPaddingLeft + systemBars.left,
                    initialPaddingTop + systemBars.top,
                    initialPaddingRight + systemBars.right,
                    initialPaddingBottom + systemBars.bottom
                )
                insets
            }
        }

        initViews()
        setupDropdown()
        checkIntent()
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
        val statusItems = arrayOf("Aktif", "Non Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusItems)
        status_kategori_auto_complete.setAdapter(adapter)
        status_kategori_auto_complete.setText("Aktif", false)
    }

    @Suppress("DEPRECATION")
    private fun checkIntent() {
        if (intent.hasExtra("IS_EDIT")) {
            isEdit = intent.getBooleanExtra("IS_EDIT", false)
        }
        
        if (isEdit) {
            ettambahkategori.text = "Edit Kategori"
            existingKategori = intent.getParcelableExtra("KATEGORI_DATA")
            
            existingKategori?.let {
                nama_kategori_edit_text.setText(it.namaKategori)
                status_kategori_auto_complete.setText(if (it.statusKategori == true) "Aktif" else "Non Aktif", false)
            }
        } else {
            ettambahkategori.text = "Tambah Kategori"
        }
    }

    private fun setupListeners() {
        button_simpan.setOnClickListener {
            saveKategori()
        }
    }

    private fun saveKategori() {
        val namaKategori = nama_kategori_edit_text.text.toString().trim()
        val statusStr = status_kategori_auto_complete.text.toString().trim()

        if (namaKategori.isEmpty()) {
            nama_kategori_layout.error = "Nama kategori tidak boleh kosong"
            return
        } else {
            nama_kategori_layout.error = null
        }

        val isActive = statusStr == "Aktif"
        val kategoriId = if (isEdit) existingKategori?.idKategori ?: return else myRef.push().key ?: return
        
        val modelKategori = ModelKategori(
            idKategori = kategoriId,
            namaKategori = namaKategori,
            statusKategori = isActive
        )

        myRef.child(kategoriId).setValue(modelKategori)
            .addOnSuccessListener {
                val msg = if (isEdit) "Kategori berhasil diupdate" else "Kategori berhasil ditambahkan"
                
                // Catat log histori aktivitas ke Firebase
                com.byatara.penjualandev.utils.CatatanHistori.catat(
                    judul = if (isEdit) "Kategori Diubah" else "Kategori Ditambahkan",
                    deskripsi = if (isEdit) "Kategori '${existingKategori?.namaKategori}' diubah menjadi '${namaKategori}'" else "Menambahkan kategori baru '${namaKategori}'",
                    tipe = "kategori"
                )

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
