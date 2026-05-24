package com.byatara.penjualandev.pegawai

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.R
import com.byatara.penjualandev.model.ModelPegawai
import com.byatara.penjualandev.utils.CrudDeleteHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class DetailPegawaiActivity : AppCompatActivity() {

    private var pegawai: ModelPegawai? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_pegawai)

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

        pegawai = intent.getParcelableExtra("PEGAWAI_DATA")
        if (pegawai == null) {
            Toast.makeText(this, "Data pegawai tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindData(pegawai!!)
        setupActions()
    }

    private fun bindData(pegawai: ModelPegawai) {
        findViewById<TextView>(R.id.tv_detail_name).text = pegawai.namaPegawai ?: "-"
        findViewById<TextView>(R.id.tv_detail_jabatan).text = pegawai.jabatanPegawai ?: "-"
        findViewById<TextView>(R.id.tv_detail_address).text = pegawai.alamatPegawai ?: "-"
        findViewById<TextView>(R.id.tv_detail_phone).text = pegawai.teleponPegawai ?: "-"
        findViewById<TextView>(R.id.tv_detail_cabang).text = pegawai.idCabang ?: "-"
        findViewById<TextView>(R.id.tv_detail_joined).text =
            "Bergabung pada ${pegawai.tanggalBergabung ?: "-"}"
    }

    private fun setupActions() {
        val data = pegawai ?: return

        findViewById<MaterialButton>(R.id.btn_detail_hubungi).setOnClickListener {
            val phone = data.teleponPegawai
            if (!phone.isNullOrEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            } else {
                Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_detail_edit).setOnClickListener {
            val intent = Intent(this, ModPegawaiActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("PEGAWAI_DATA", data)
            }
            startActivity(intent)
        }

        findViewById<MaterialButton>(R.id.btn_detail_hapus).setOnClickListener {
            val id = data.idPegawai ?: return@setOnClickListener
            val nama = data.namaPegawai ?: "pegawai ini"
            CrudDeleteHelper.confirmAndDelete(
                activity = this,
                title = "Hapus Pegawai",
                message = "Apakah Anda yakin ingin menghapus \"$nama\"?",
                firebasePath = "pegawai",
                itemId = id,
                historiJudul = "Pegawai Dihapus",
                historiDeskripsi = "Menghapus pegawai '$nama'",
                historiTipe = "pegawai",
                onSuccess = { finish() }
            )
        }
    }
}
