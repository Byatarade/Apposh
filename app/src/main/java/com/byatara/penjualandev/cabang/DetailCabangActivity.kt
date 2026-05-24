package com.byatara.penjualandev.cabang

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
import com.byatara.penjualandev.model.ModelCabang
import com.byatara.penjualandev.utils.CrudDeleteHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip

class DetailCabangActivity : AppCompatActivity() {

    private var cabang: ModelCabang? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_cabang)

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

        cabang = intent.getParcelableExtra("CABANG_DATA")
        if (cabang == null) {
            Toast.makeText(this, "Data cabang tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindData(cabang!!)
        setupActions()
    }

    private fun bindData(cabang: ModelCabang) {
        findViewById<TextView>(R.id.tv_detail_name).text = cabang.namaCabang ?: "-"
        findViewById<TextView>(R.id.tv_detail_alamat).text = cabang.alamatCabang ?: "-"
        findViewById<TextView>(R.id.tv_detail_phone).text = cabang.teleponCabang ?: "-"

        val chipStatus = findViewById<Chip>(R.id.chip_detail_status)
        chipStatus.text = if (cabang.statusCabang == true) "Aktif" else "Non Aktif"
    }

    private fun setupActions() {
        val data = cabang ?: return

        findViewById<MaterialButton>(R.id.btn_detail_edit).setOnClickListener {
            val intent = Intent(this, ModCabangActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("CABANG_DATA", data)
            }
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btn_detail_hapus).setOnClickListener {
            val id = data.idCabang ?: return@setOnClickListener
            val nama = data.namaCabang ?: "cabang ini"
            CrudDeleteHelper.confirmAndDelete(
                activity = this,
                title = "Hapus Cabang",
                message = "Apakah Anda yakin ingin menghapus \"$nama\"?",
                firebasePath = "cabang",
                itemId = id,
                historiJudul = "Cabang Dihapus",
                historiDeskripsi = "Menghapus cabang '$nama'",
                historiTipe = "cabang",
                onSuccess = { finish() }
            )
        }
    }
}
