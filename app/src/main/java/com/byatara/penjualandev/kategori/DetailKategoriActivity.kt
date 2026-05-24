package com.byatara.penjualandev.kategori

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelKategori
import com.byatara.penjualandev.utils.CrudDeleteHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class DetailKategoriActivity : AppCompatActivity() {

    private var kategori: ModelKategori? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_kategori)

        val mainView = findViewById<View>(android.R.id.content)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        kategori = intent.getParcelableExtra("KATEGORI_DATA")
        if (kategori == null) {
            Toast.makeText(this, "Data kategori tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindData(kategori!!)
        setupActions()
    }

    private fun bindData(kategori: ModelKategori) {
        findViewById<TextView>(R.id.tv_detail_name).text = kategori.namaKategori ?: "-"

        val chipStatus = findViewById<Chip>(R.id.chip_detail_status)
        chipStatus.text = if (kategori.statusKategori == true) "Aktif" else "Non Aktif"
    }

    private fun setupActions() {
        val data = kategori ?: return

        findViewById<MaterialButton>(R.id.btn_detail_edit).setOnClickListener {
            val intent = Intent(this, ModKategoriActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("KATEGORI_DATA", data)
            }
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btn_detail_hapus).setOnClickListener {
            val id = data.idKategori ?: return@setOnClickListener
            val nama = data.namaKategori ?: "kategori ini"
            CrudDeleteHelper.confirmAndDelete(
                activity = this,
                title = "Hapus Kategori",
                message = "Apakah Anda yakin ingin menghapus \"$nama\"?",
                firebasePath = "kategori",
                itemId = id,
                historiJudul = "Kategori Dihapus",
                historiDeskripsi = "Menghapus kategori '$nama'",
                historiTipe = "kategori",
                onSuccess = { finish() }
            )
        }
    }
}
