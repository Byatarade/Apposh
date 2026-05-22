package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class PembayaranActivity : AppCompatActivity() {

    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var btnConfirmPayment: MaterialButton

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_pembayaran)

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

        tvTotalPrice = findViewById(R.id.tv_total_price)
        tvTotalItems = findViewById(R.id.tv_total_items)
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment)

        val totalItems = intent.getIntExtra("TOTAL_ITEMS", 0)
        val totalPrice = intent.getIntExtra("TOTAL_PRICE", 0)

        tvTotalItems.text = "Total: $totalItems Item"
        tvTotalPrice.text = formatRupiah(totalPrice)

        btnConfirmPayment.setOnClickListener {
            handleConfirmation()
        }
    }

    private fun handleConfirmation() {
        android.app.AlertDialog.Builder(this)
            .setTitle("Pembayaran Berhasil")
            .setMessage("Transaksi Anda berhasil diproses. Terima kasih!")
            .setPositiveButton("OK") { dialog, _ ->
                dialog.dismiss()
                // Return to TransaksiActivity and clear cart, or just go back to Main
                val intent = Intent(this, TransaksiActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }
}
