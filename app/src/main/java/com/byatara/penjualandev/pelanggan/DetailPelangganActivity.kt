package com.byatara.penjualandev.pelanggan

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
import com.byatara.penjualandev.model.ModelPelanggan
import com.byatara.penjualandev.utils.CrudDeleteHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailPelangganActivity : AppCompatActivity() {

    private var pelanggan: ModelPelanggan? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_detail_pelanggan)

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

        pelanggan = intent.getParcelableExtra("PELANGGAN_DATA")
        if (pelanggan == null) {
            Toast.makeText(this, "Data pelanggan tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindData(pelanggan!!)
        setupActions()
    }

    private fun bindData(pelanggan: ModelPelanggan) {
        findViewById<TextView>(R.id.tv_detail_name).text = pelanggan.namaPelanggan ?: "-"
        findViewById<TextView>(R.id.tv_detail_phone).text = pelanggan.teleponPelanggan ?: "-"
        findViewById<TextView>(R.id.tv_detail_address).text = pelanggan.alamatPelanggan ?: "-"

        val chipStatus = findViewById<Chip>(R.id.chip_detail_status)
        chipStatus.text = if (pelanggan.statusPelanggan == true) "Aktif" else "Non Aktif"

        val createdAt = pelanggan.createdAt
        val createdText = if (createdAt != null && createdAt > 0L) {
            val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
            "Terdaftar pada ${sdf.format(Date(createdAt))}"
        } else {
            "Terdaftar pada -"
        }
        findViewById<TextView>(R.id.tv_detail_created).text = createdText
    }

    private fun setupActions() {
        val data = pelanggan ?: return

        findViewById<MaterialButton>(R.id.btn_detail_hubungi).setOnClickListener {
            val phone = data.teleponPelanggan
            if (!phone.isNullOrEmpty()) {
                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone")))
            } else {
                Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
            }
        }

        findViewById<MaterialButton>(R.id.btn_detail_edit).setOnClickListener {
            startActivity(Intent(this, ModPelangganActivity::class.java).apply {
                putExtra("IS_EDIT", true)
                putExtra("PELANGGAN_DATA", data)
            })
        }

        findViewById<MaterialButton>(R.id.btn_detail_hapus).setOnClickListener {
            val id = data.idPelanggan ?: return@setOnClickListener
            val nama = data.namaPelanggan ?: "pelanggan ini"
            CrudDeleteHelper.confirmAndDelete(
                activity = this,
                title = "Hapus Pelanggan",
                message = "Apakah Anda yakin ingin menghapus \"$nama\"?",
                firebasePath = "pelanggan",
                itemId = id,
                historiJudul = "Pelanggan Dihapus",
                historiDeskripsi = "Menghapus pelanggan '$nama'",
                historiTipe = "pelanggan",
                onSuccess = { finish() }
            )
        }
    }
}
