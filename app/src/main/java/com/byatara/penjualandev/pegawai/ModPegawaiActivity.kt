package com.byatara.penjualandev.pegawai

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
import com.byatara.penjualandev.model.ModelPegawai
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.database.FirebaseDatabase

class ModPegawaiActivity : AppCompatActivity() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    private lateinit var tvTitle: TextView
    private lateinit var namaPegawaiLayout: TextInputLayout
    private lateinit var namaPegawaiEditText: TextInputEditText
    private lateinit var jabatanPegawaiLayout: TextInputLayout
    private lateinit var jabatanPegawaiEditText: TextInputEditText
    private lateinit var teleponPegawaiLayout: TextInputLayout
    private lateinit var teleponPegawaiEditText: TextInputEditText
    private lateinit var statusPegawaiLayout: TextInputLayout
    private lateinit var statusPegawaiAutoComplete: AutoCompleteTextView
    private lateinit var buttonSimpan: Button

    private var isEdit = false
    private var existingPegawai: ModelPegawai? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_mod_pegawai)
        
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
        namaPegawaiLayout = findViewById(R.id.nama_pegawai_layout)
        namaPegawaiEditText = findViewById(R.id.nama_pegawai_edit_text)
        jabatanPegawaiLayout = findViewById(R.id.jabatan_pegawai_layout)
        jabatanPegawaiEditText = findViewById(R.id.jabatan_pegawai_edit_text)
        teleponPegawaiLayout = findViewById(R.id.telepon_pegawai_layout)
        teleponPegawaiEditText = findViewById(R.id.telepon_pegawai_edit_text)
        statusPegawaiLayout = findViewById(R.id.status_pegawai_layout)
        statusPegawaiAutoComplete = findViewById(R.id.status_pegawai_auto_complete)
        buttonSimpan = findViewById(R.id.button_simpan)
    }

    private fun setupDropdown() {
        val statusItems = arrayOf("Aktif", "Non Aktif")
        val adapter = ArrayAdapter(this, android.R.layout.simple_dropdown_item_1line, statusItems)
        statusPegawaiAutoComplete.setAdapter(adapter)
        statusPegawaiAutoComplete.setText("Aktif", false)
    }

    @Suppress("DEPRECATION")
    private fun checkIntent() {
        if (intent.hasExtra("IS_EDIT")) {
            isEdit = intent.getBooleanExtra("IS_EDIT", false)
        }
        
        if (isEdit) {
            tvTitle.text = "Edit Pegawai"
            existingPegawai = intent.getParcelableExtra("PEGAWAI_DATA")
            
            existingPegawai?.let {
                namaPegawaiEditText.setText(it.namaPegawai)
                jabatanPegawaiEditText.setText(it.jabatanPegawai)
                teleponPegawaiEditText.setText(it.teleponPegawai)
                statusPegawaiAutoComplete.setText(if (it.statusPegawai == true) "Aktif" else "Non Aktif", false)
            }
        } else {
            tvTitle.text = "Tambah Pegawai"
        }
    }

    private fun setupListeners() {
        buttonSimpan.setOnClickListener {
            savePegawai()
        }
    }

    private fun savePegawai() {
        val namaPegawai = namaPegawaiEditText.text.toString().trim()
        val jabatanPegawai = jabatanPegawaiEditText.text.toString().trim()
        val teleponPegawai = teleponPegawaiEditText.text.toString().trim()
        val statusStr = statusPegawaiAutoComplete.text.toString().trim()

        var isValid = true

        if (namaPegawai.isEmpty()) {
            namaPegawaiLayout.error = "Nama pegawai tidak boleh kosong"
            isValid = false
        } else {
            namaPegawaiLayout.error = null
        }

        if (jabatanPegawai.isEmpty()) {
            jabatanPegawaiLayout.error = "Jabatan tidak boleh kosong"
            isValid = false
        } else {
            jabatanPegawaiLayout.error = null
        }
        
        if (teleponPegawai.isEmpty()) {
            teleponPegawaiLayout.error = "Telepon tidak boleh kosong"
            isValid = false
        } else {
            teleponPegawaiLayout.error = null
        }

        if (statusStr.isEmpty()) {
            statusPegawaiLayout.error = "Status pegawai harus dipilih"
            isValid = false
        } else {
            statusPegawaiLayout.error = null
        }

        if (!isValid) return

        val isActive = statusStr == "Aktif"
        
        val pegawaiId = if (isEdit) existingPegawai?.idPegawai ?: return else myRef.push().key ?: return
        
        val modelPegawai = ModelPegawai(
            idPegawai = pegawaiId,
            namaPegawai = namaPegawai,
            jabatanPegawai = jabatanPegawai,
            teleponPegawai = teleponPegawai,
            statusPegawai = isActive
        )

        myRef.child(pegawaiId).setValue(modelPegawai)
            .addOnSuccessListener {
                val msg = if (isEdit) "Berhasil mengupdate pegawai" else "Berhasil menambahkan pegawai"
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Gagal: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
