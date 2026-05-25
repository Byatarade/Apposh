package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.adapter.OrderSummaryAdapter
import com.byatara.penjualandev.model.ModelOrder
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.utils.BluetoothPermissionHelper
import com.byatara.penjualandev.utils.BluetoothPrinterHelper
import com.google.android.material.button.MaterialButton
import java.text.NumberFormat
import java.util.Locale

class ReceiptActivity : AppCompatActivity() {

    private lateinit var tvReceiptId: TextView
    private lateinit var tvReceiptTime: TextView
    private lateinit var tvReceiptCashier: TextView
    private lateinit var tvReceiptCustomer: TextView
    private lateinit var llReceiptItems: LinearLayout
    
    private lateinit var tvReceiptSubtotal: TextView
    private lateinit var tvReceiptTax: TextView
    private lateinit var tvReceiptTotal: TextView
    
    private lateinit var tvReceiptPayMethod: TextView
    private lateinit var tvReceiptAmountPaid: TextView
    private lateinit var tvReceiptChange: TextView
    private lateinit var tvReceiptMethodLabel: TextView

    private lateinit var btnShareReceipt: MaterialButton
    private lateinit var btnPrintReceipt: MaterialButton
    private lateinit var btnReceiptDone: MaterialButton

    private var currentOrder: ModelOrder? = null
    private var pendingPrintOrder: ModelOrder? = null

    private val bluetoothPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions.values.all { it }
        if (granted) {
            pendingPrintOrder?.let { startPrint(it) }
        } else {
            Toast.makeText(this, "Izin Bluetooth diperlukan untuk mencetak", Toast.LENGTH_LONG).show()
        }
        pendingPrintOrder = null
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_receipt)

        val mainView = findViewById<View>(android.R.id.content)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        
        currentOrder = intent.getParcelableExtra("ORDER_DATA")
        if (currentOrder == null) {
            Toast.makeText(this, "Data order tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        populateReceiptData(currentOrder!!)
        
        btnReceiptDone.setOnClickListener {
            // Clean stack and open TransaksiActivity freshly
            val intent = Intent(this, TransaksiActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }

        btnShareReceipt.setOnClickListener {
            shareReceiptText(currentOrder!!)
        }

        btnPrintReceipt.setOnClickListener {
            requestPrint(currentOrder!!)
        }
    }

    private fun initViews() {
        tvReceiptId = findViewById(R.id.tv_receipt_id)
        tvReceiptTime = findViewById(R.id.tv_receipt_time)
        tvReceiptCashier = findViewById(R.id.tv_receipt_cashier)
        tvReceiptCustomer = findViewById(R.id.tv_receipt_customer)
        llReceiptItems = findViewById(R.id.ll_receipt_items)
        
        tvReceiptSubtotal = findViewById(R.id.tv_receipt_subtotal)
        tvReceiptTax = findViewById(R.id.tv_receipt_tax)
        tvReceiptTotal = findViewById(R.id.tv_receipt_total)
        
        tvReceiptPayMethod = findViewById(R.id.tv_receipt_pay_method)
        tvReceiptAmountPaid = findViewById(R.id.tv_receipt_amount_paid)
        tvReceiptChange = findViewById(R.id.tv_receipt_change)
        tvReceiptMethodLabel = findViewById(R.id.tv_receipt_method_label)

        btnShareReceipt = findViewById(R.id.btn_share_receipt)
        btnPrintReceipt = findViewById(R.id.btn_print_receipt)
        btnReceiptDone = findViewById(R.id.btn_receipt_done)
    }

    private fun requestPrint(order: ModelOrder) {
        if (BluetoothPermissionHelper.hasAllPermissions(this)) {
            startPrint(order)
        } else {
            pendingPrintOrder = order
            bluetoothPermissionLauncher.launch(BluetoothPermissionHelper.requiredPermissions())
        }
    }

    private fun startPrint(order: ModelOrder) {
        btnPrintReceipt.isEnabled = false
        Toast.makeText(this, "Menghubungkan ke printer...", Toast.LENGTH_SHORT).show()
        BluetoothPrinterHelper.printReceipt(order) { success, message ->
            btnPrintReceipt.isEnabled = true
            Toast.makeText(this, message, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
        }
    }

    private fun populateReceiptData(order: ModelOrder) {
        tvReceiptId.text = "Order ID: ${order.idOrder}"
        tvReceiptTime.text = order.tanggalWaktu ?: "-"
        tvReceiptCashier.text = order.namaKasir ?: "Kasir"
        tvReceiptCustomer.text = if (order.namaPelanggan.isNullOrEmpty()) "Umum" else order.namaPelanggan

        tvReceiptSubtotal.text = formatRupiah(order.subtotal ?: 0)
        tvReceiptTax.text = formatRupiah(order.pajak ?: 0)
        tvReceiptTotal.text = formatRupiah(order.totalHarga ?: 0)

        val method = order.metodeBayar ?: "Tunai"
        tvReceiptPayMethod.text = method
        tvReceiptMethodLabel.text = "Metode Pembayaran ($method)"

        if (method.lowercase() == "tunai") {
            tvReceiptAmountPaid.text = formatRupiah(order.uangDiterima ?: 0)
            tvReceiptChange.text = formatRupiah(order.kembalian ?: 0)
        } else {
            tvReceiptAmountPaid.text = formatRupiah(order.totalHarga ?: 0)
            tvReceiptChange.text = "Rp 0"
        }

        // Dynamically add products to layout
        llReceiptItems.removeAllViews()
        order.items?.forEach { item ->
            val itemView = LayoutInflater.from(this).inflate(R.layout.item_order_summary, llReceiptItems, false)
            val tvName = itemView.findViewById<TextView>(R.id.tv_summary_name)
            val tvQtyPrice = itemView.findViewById<TextView>(R.id.tv_summary_qty_price)
            val tvSubtotal = itemView.findViewById<TextView>(R.id.tv_summary_subtotal)

            tvName.text = item.namaProduk ?: "-"
            tvQtyPrice.text = "${item.qty} x ${formatRupiah(item.hargaJual ?: 0)}"
            tvSubtotal.text = formatRupiah(item.subtotal ?: 0)

            llReceiptItems.addView(itemView)
        }
    }

    private fun shareReceiptText(order: ModelOrder) {
        val storeHeader = "=== DEVSPARK POS ===\n" +
                          "Solusi Point of Sale Pintar\n" +
                          "====================\n\n"
        
        val metaInfo = "Order ID : ${order.idOrder}\n" +
                       "Waktu    : ${order.tanggalWaktu}\n" +
                       "Kasir    : ${order.namaKasir ?: "-"}\n" +
                       "Pelanggan: ${if (order.namaPelanggan.isNullOrEmpty()) "Umum" else order.namaPelanggan}\n" +
                       "--------------------\n\n"

        val itemHeader = "Daftar Item:\n"
        val builder = StringBuilder().append(storeHeader).append(metaInfo).append(itemHeader)

        order.items?.forEach { item ->
            builder.append("- ${item.namaProduk} (${item.qty}x) : ${formatRupiah(item.subtotal ?: 0)}\n")
        }

        val footer = "\n--------------------\n" +
                     "Subtotal : ${formatRupiah(order.subtotal ?: 0)}\n" +
                     "PPN 11%  : ${formatRupiah(order.pajak ?: 0)}\n" +
                     "Total    : ${formatRupiah(order.totalHarga ?: 0)}\n" +
                     "Metode   : ${order.metodeBayar}\n" +
                     "Bayar    : ${formatRupiah(if (order.metodeBayar?.lowercase() == "tunai") order.uangDiterima ?: 0 else order.totalHarga ?: 0)}\n" +
                     "Kembali  : ${formatRupiah(if (order.metodeBayar?.lowercase() == "tunai") order.kembalian ?: 0 else 0)}\n\n" +
                     "====================\n" +
                     "Terima Kasih Atas Kunjungan Anda!"

        val shareText = builder.append(footer).toString()

        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
        }
        startActivity(Intent.createChooser(intent, "Bagikan Struk Melalui"))
    }
}
