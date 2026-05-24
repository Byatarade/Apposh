package com.byatara.penjualandev.pelanggan

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
import com.byatara.penjualandev.model.ModelPelanggan
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ModPelangganActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")

    private lateinit var tvTitle: TextView
    private lateinit var namaPelangganLayout: TextInputLayout
    private lateinit var namaPelangganEditText: TextInputEditText
    private lateinit var teleponPelangganLayout: TextInputLayout
    private lateinit var teleponPelangganEditText: TextInputEditText
    private lateinit var alamatPelangganLayout: TextInputLayout
    private lateinit var alamatPelangganEditText: TextInputEditText
    private lateinit var statusPelangganLayout: TextInputLayout
    private lateinit var statusPelangganAutoComplete: AutoCompleteTextView
    private lateinit var buttonSimpan: Button

    private var isEdit = false
    private var existingPelanggan: ModelPelanggan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pelanggan)

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
        tvTitle = findViewById(R.id.tv_title)
        namaPelangganLayout = findViewById(R.id.nama_pelanggan_layout)
        namaPelangganEditText = findViewById(R.id.nama_pelanggan_edit_text)
        teleponPelangganLayout = findViewById(R.id.telepon_pelanggan_layout)
        teleponPelangganEditText = findViewById(R.id.telepon_pelanggan_edit_text)
        alamatPelangganLayout = findViewById(R.id.alamat_pelanggan_layout)
        alamatPelangganEditText = findViewById(R.id.alamat_pelanggan_edit_text)
        statusPelangganLayout = findViewById(R.id.status_pelanggan_layout)
        statusPelangganAutoComplete = findViewById(R.id.status_pelanggan_auto_complete)
        buttonSimpan = findViewById(R.id.button_simpan)
    }

    private fun setupDropdown() {
        val statusItems = arrayOf("Aktif", "Non Aktif")
        val statusAdapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusItems)
        statusPelangganAutoComplete.setAdapter(statusAdapter)
        statusPelangganAutoComplete.setText("Aktif", false)
    }

    @Suppress("DEPRECATION")
    private fun checkIntent() {
        if (intent.hasExtra("IS_EDIT")) {
            isEdit = intent.getBooleanExtra("IS_EDIT", false)
        }

        if (isEdit) {
            tvTitle.text = "Edit Pelanggan"
            existingPelanggan = intent.getParcelableExtra("PELANGGAN_DATA")

            existingPelanggan?.let {
                namaPelangganEditText.setText(it.namaPelanggan)
                teleponPelangganEditText.setText(it.teleponPelanggan)
                alamatPelangganEditText.setText(it.alamatPelanggan)
                statusPelangganAutoComplete.setText(if (it.statusPelanggan == true) "Aktif" else "Non Aktif", false)
            }
        } else {
            tvTitle.text = "Tambah Pelanggan"
        }
    }

    private fun setupListeners() {
        buttonSimpan.setOnClickListener {
            savePelanggan()
        }
    }

    private fun savePelanggan() {
        val namaPelanggan = namaPelangganEditText.text.toString().trim()
        val teleponPelanggan = teleponPelangganEditText.text.toString().trim()
        val alamatPelanggan = alamatPelangganEditText.text.toString().trim()
        val statusStr = statusPelangganAutoComplete.text.toString().trim()

        var isValid = true

        if (namaPelanggan.isEmpty()) {
            namaPelangganLayout.error = "Nama pelanggan tidak boleh kosong"
            isValid = false
        } else {
            namaPelangganLayout.error = null
        }

        if (teleponPelanggan.isEmpty()) {
            teleponPelangganLayout.error = "Telepon tidak boleh kosong"
            isValid = false
        } else {
            teleponPelangganLayout.error = null
        }

        if (alamatPelanggan.isEmpty()) {
            alamatPelangganLayout.error = "Alamat tidak boleh kosong"
            isValid = false
        } else {
            alamatPelangganLayout.error = null
        }

        if (statusStr.isEmpty()) {
            statusPelangganLayout.error = "Status pelanggan harus dipilih"
            isValid = false
        } else {
            statusPelangganLayout.error = null
        }

        if (!isValid) return

        val isActive = statusStr == "Aktif"

        val pelangganId = if (isEdit) existingPelanggan?.idPelanggan ?: return else myRef.push().key ?: return
        val createdAt = if (isEdit) existingPelanggan?.createdAt ?: System.currentTimeMillis() else System.currentTimeMillis()

        val modelPelanggan = ModelPelanggan(
            idPelanggan = pelangganId,
            namaPelanggan = namaPelanggan,
            teleponPelanggan = teleponPelanggan,
            alamatPelanggan = alamatPelanggan,
            statusPelanggan = isActive,
            createdAt = createdAt
        )

        myRef.child(pelangganId).setValue(modelPelanggan)
            .addOnSuccessListener {
                val msg = if (isEdit) "Berhasil mengupdate pelanggan" else "Berhasil menambahkan pelanggan"

                com.byatara.penjualandev.utils.CatatanHistori.catat(
                    judul = if (isEdit) "Pelanggan Diubah" else "Pelanggan Ditambahkan",
                    deskripsi = if (isEdit) "Pelanggan '${existingPelanggan?.namaPelanggan}' diubah menjadi '${namaPelanggan}'" else "Menambahkan pelanggan baru '${namaPelanggan}'",
                    tipe = "pelanggan"
                )

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
