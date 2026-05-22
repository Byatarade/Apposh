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
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton

class DetailPegawaiActivity : AppCompatActivity() {

    private lateinit var tvName: TextView
    private lateinit var tvJabatan: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvPhone: TextView
    private lateinit var tvCabang: TextView
    private lateinit var tvJoined: TextView
    private lateinit var btnHubungi: MaterialButton

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

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        tvName = findViewById(R.id.tv_detail_name)
        tvJabatan = findViewById(R.id.tv_detail_jabatan)
        tvAddress = findViewById(R.id.tv_detail_address)
        tvPhone = findViewById(R.id.tv_detail_phone)
        tvCabang = findViewById(R.id.tv_detail_cabang)
        tvJoined = findViewById(R.id.tv_detail_joined)
        btnHubungi = findViewById(R.id.btn_detail_hubungi)

        val pegawai = intent.getParcelableExtra<ModelPegawai>("PEGAWAI_DATA")
        if (pegawai != null) {
            tvName.text = pegawai.namaPegawai ?: "-"
            tvJabatan.text = pegawai.jabatanPegawai ?: "-"
            tvAddress.text = pegawai.alamatPegawai ?: "-"
            tvPhone.text = pegawai.teleponPegawai ?: "-"
            tvCabang.text = pegawai.idCabang ?: "-"
            tvJoined.text = "Bergabung pada ${pegawai.tanggalBergabung ?: "-"}"

            btnHubungi.setOnClickListener {
                val phone = pegawai.teleponPegawai
                if (!phone.isNullOrEmpty()) {
                    val dialIntent = Intent(Intent.ACTION_DIAL, Uri.parse("tel:$phone"))
                    startActivity(dialIntent)
                } else {
                    Toast.makeText(this, "Nomor telepon tidak tersedia", Toast.LENGTH_SHORT).show()
                }
            }
        } else {
            Toast.makeText(this, "Data pegawai tidak ditemukan", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}
