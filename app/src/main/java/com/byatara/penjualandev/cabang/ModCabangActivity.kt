package com.byatara.penjualandev.cabang

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelCabang
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ModCabangActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    private lateinit var tvTitle: TextView
    private lateinit var namaCabangLayout: TextInputLayout
    private lateinit var namaCabangEditText: TextInputEditText
    private lateinit var alamatCabangLayout: TextInputLayout
    private lateinit var alamatCabangEditText: TextInputEditText
    private lateinit var teleponCabangLayout: TextInputLayout
    private lateinit var teleponCabangEditText: TextInputEditText
    private lateinit var switchStatusCabang: MaterialSwitch
    private lateinit var buttonSimpan: Button


    private var isEdit = false
    private var existingCabang: ModelCabang? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_cabang)
        
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
        tvTitle = findViewById(R.id.tv_title)
        namaCabangLayout = findViewById(R.id.nama_cabang_layout)
        namaCabangEditText = findViewById(R.id.nama_cabang_edit_text)
        alamatCabangLayout = findViewById(R.id.alamat_cabang_layout)
        alamatCabangEditText = findViewById(R.id.alamat_cabang_edit_text)
        teleponCabangLayout = findViewById(R.id.telepon_cabang_layout)
        teleponCabangEditText = findViewById(R.id.telepon_cabang_edit_text)
        switchStatusCabang = findViewById(R.id.switch_status_cabang)
        buttonSimpan = findViewById(R.id.button_simpan)
    }

    @Suppress("DEPRECATION")
    private fun checkIntent() {
        if (intent.hasExtra("IS_EDIT")) {
            isEdit = intent.getBooleanExtra("IS_EDIT", false)
        }
        
        if (isEdit) {
            tvTitle.text = "Edit Cabang"
            existingCabang = intent.getParcelableExtra<ModelCabang>("CABANG_DATA")
            
            existingCabang?.let {
                namaCabangEditText.setText(it.namaCabang)
                alamatCabangEditText.setText(it.alamatCabang)
                teleponCabangEditText.setText(it.teleponCabang)
                switchStatusCabang.isChecked = it.statusCabang == true
            }
        } else {
            tvTitle.text = "Tambah Cabang"
            switchStatusCabang.isChecked = true
        }
    }

    private fun setupListeners() {
        buttonSimpan.setOnClickListener {
            saveCabang()
        }
    }

    private fun saveCabang() {
        val namaCabang = namaCabangEditText.text.toString().trim()
        val alamatCabang = alamatCabangEditText.text.toString().trim()
        val teleponCabang = teleponCabangEditText.text.toString().trim()
        val isActive = switchStatusCabang.isChecked

        var isValid = true

        if (namaCabang.isEmpty()) {
            namaCabangLayout.error = "Nama cabang tidak boleh kosong"
            isValid = false
        } else {
            namaCabangLayout.error = null
        }

        if (alamatCabang.isEmpty()) {
            alamatCabangLayout.error = "Alamat cabang tidak boleh kosong"
            isValid = false
        } else {
            alamatCabangLayout.error = null
        }
        
        if (teleponCabang.isEmpty()) {
            teleponCabangLayout.error = "Telepon cabang tidak boleh kosong"
            isValid = false
        } else {
            teleponCabangLayout.error = null
        }

        if (!isValid) return
        
        val cabangId = if (isEdit) existingCabang?.idCabang ?: return else myRef.push().key ?: return
        
        val modelCabang = ModelCabang(
            idCabang = cabangId,
            namaCabang = namaCabang,
            alamatCabang = alamatCabang,
            teleponCabang = teleponCabang,
            statusCabang = isActive
        )

        myRef.child(cabangId).setValue(modelCabang)
            .addOnSuccessListener {
                val msg = if (isEdit) "Berhasil mengupdate cabang" else "Berhasil menambahkan cabang"
                
                // Catat log histori aktivitas ke Firebase
                com.byatara.penjualandev.utils.CatatanHistori.catat(
                    judul = if (isEdit) "Cabang Diubah" else "Cabang Ditambahkan",
                    deskripsi = if (isEdit) "Cabang '${existingCabang?.namaCabang}' diubah menjadi '${namaCabang}'" else "Menambahkan cabang baru '${namaCabang}'",
                    tipe = "cabang"
                )

                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
