package com.byatara.penjualandev.kategori

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelKategori
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ModKategoriActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    private lateinit var ettambahkategori: TextView
    private lateinit var nama_kategori_layout: TextInputLayout
    private lateinit var nama_kategori_edit_text: TextInputEditText
    private lateinit var switchStatusKategori: MaterialSwitch
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
        checkIntent()
        setupListeners()
    }

    private fun initViews() {
        ettambahkategori = findViewById(R.id.ettambahkategori)
        nama_kategori_layout = findViewById(R.id.nama_kategori_layout)
        nama_kategori_edit_text = findViewById(R.id.nama_kategori_edit_text)
        switchStatusKategori = findViewById(R.id.switch_status_kategori)
        button_simpan = findViewById(R.id.button_simpan)
    }

    @Suppress("DEPRECATION")
    private fun checkIntent() {
        if (intent.hasExtra("IS_EDIT")) {
            isEdit = intent.getBooleanExtra("IS_EDIT", false)
        }
        
        if (isEdit) {
            ettambahkategori.text = "Edit Kategori"
            existingKategori = intent.getParcelableExtra<ModelKategori>("KATEGORI_DATA")
            
            existingKategori?.let {
                nama_kategori_edit_text.setText(it.namaKategori)
                switchStatusKategori.isChecked = it.statusKategori == true
            }
        } else {
            ettambahkategori.text = "Tambah Kategori"
            switchStatusKategori.isChecked = true
        }
    }

    private fun setupListeners() {
        button_simpan.setOnClickListener {
            saveKategori()
        }
    }

    private fun saveKategori() {
        val namaKategori = nama_kategori_edit_text.text.toString().trim()
        val isActive = switchStatusKategori.isChecked

        if (namaKategori.isEmpty()) {
            nama_kategori_layout.error = "Nama kategori tidak boleh kosong"
            return
        } else {
            nama_kategori_layout.error = null
        }

        val kategoriId = if (isEdit) existingKategori?.idKategori ?: return else myRef.push().key ?: return
        
        val modelKategori = ModelKategori(
            idKategori = kategoriId,
            namaKategori = namaKategori,
            statusKategori = isActive
        )

        myRef.child(kategoriId).setValue(modelKategori)
            .addOnSuccessListener {
                val msg = if (isEdit) "Berhasil mengupdate kategori" else "Berhasil menambahkan kategori"
                
                // Catat log histori aktivitas ke Firebase
                com.byatara.penjualandev.utils.CatatanHistori.catat(
                    judul = if (isEdit) "Kategori Diubah" else "Kategori Ditambahkan",
                    deskripsi = if (isEdit) "Kategori '${existingKategori?.namaKategori}' diubah menjadi '${namaKategori}'" else "Menambahkan kategori baru '${namaKategori}'",
                    tipe = "kategori"
                )

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { 
                Toast.makeText(this, "Gagal menyimpan kategori", Toast.LENGTH_SHORT).show()
            }
    }
}
