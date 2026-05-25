package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.OrderSummaryAdapter
import com.byatara.penjualandev.model.ModelNotification
import com.byatara.penjualandev.model.ModelOrder
import com.byatara.penjualandev.utils.SaldoManager
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.android.material.button.MaterialButtonToggleGroup
import com.google.android.material.chip.Chip
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import java.text.NumberFormat
import java.util.Locale

class PembayaranActivity : AppCompatActivity() {

    private lateinit var tvTotalPrice: TextView
    private lateinit var tvTotalItems: TextView
    private lateinit var tvPayCashier: TextView
    private lateinit var tvPayCustomer: TextView
    private lateinit var tvPaySubtotal: TextView
    private lateinit var tvPayTax: TextView
    
    private lateinit var rvPayItems: RecyclerView
    private lateinit var togglePaymentMethod: MaterialButtonToggleGroup
    private lateinit var llCashContainer: LinearLayout
    private lateinit var llQrisContainer: LinearLayout
    private lateinit var llEwalletContainer: LinearLayout
    private lateinit var cvQrisCode: View
    private lateinit var etCashReceived: TextInputEditText
    private lateinit var tvChangeAmount: TextView
    
    private lateinit var chipExactAmount: Chip
    private lateinit var chip50k: Chip
    private lateinit var chip100k: Chip
    
    private lateinit var btnConfirmPayment: MaterialButton

    private var currentOrder: ModelOrder? = null
    private var selectedMethod = "Tunai"

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

        initViews()

        currentOrder = intent.getParcelableExtra("ORDER_DATA")
        if (currentOrder == null) {
            Toast.makeText(this, "Data pemesanan tidak ditemukan!", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        bindOrderData(currentOrder!!)
        setupRecyclerView(currentOrder!!)
        setupPaymentMethods()
        setupCashCalculator(currentOrder!!)

        btnConfirmPayment.setOnClickListener {
            handleConfirmation()
        }
    }

    private fun initViews() {
        tvTotalPrice = findViewById(R.id.tv_total_price)
        tvTotalItems = findViewById(R.id.tv_total_items)
        tvPayCashier = findViewById(R.id.tv_pay_cashier)
        tvPayCustomer = findViewById(R.id.tv_pay_customer)
        tvPaySubtotal = findViewById(R.id.tv_pay_subtotal)
        tvPayTax = findViewById(R.id.tv_pay_tax)
        
        rvPayItems = findViewById(R.id.rv_pay_items)
        togglePaymentMethod = findViewById(R.id.toggle_payment_method)
        llCashContainer = findViewById(R.id.ll_cash_container)
        llQrisContainer = findViewById(R.id.ll_qris_container)
        llEwalletContainer = findViewById(R.id.ll_ewallet_container)
        cvQrisCode = findViewById(R.id.cv_qris_code)
        etCashReceived = findViewById(R.id.et_cash_received)
        tvChangeAmount = findViewById(R.id.tv_change_amount)
        
        chipExactAmount = findViewById(R.id.chip_exact_amount)
        chip50k = findViewById(R.id.chip_50k)
        chip100k = findViewById(R.id.chip_100k)
        
        btnConfirmPayment = findViewById(R.id.btn_confirm_payment)
    }

    private fun bindOrderData(order: ModelOrder) {
        val totalQty = order.items?.sumOf { it.qty ?: 0 } ?: 0
        tvTotalItems.text = "$totalQty Item"
        tvTotalPrice.text = formatRupiah(order.totalHarga ?: 0)
        
        tvPayCashier.text = order.namaKasir ?: "Kasir Utama"
        tvPayCustomer.text = if (order.namaPelanggan.isNullOrEmpty()) "Umum" else order.namaPelanggan
        
        tvPaySubtotal.text = formatRupiah(order.subtotal ?: 0)
        tvPayTax.text = formatRupiah(order.pajak ?: 0)
    }

    private fun setupRecyclerView(order: ModelOrder) {
        val items = order.items ?: emptyList()
        val adapter = OrderSummaryAdapter(items)
        rvPayItems.layoutManager = LinearLayoutManager(this)
        rvPayItems.adapter = adapter
    }

    private fun setupPaymentMethods() {
        cvQrisCode.setOnClickListener {
            if (selectedMethod == "QRIS") {
                Toast.makeText(this, "Simulasi Pembayaran QRIS Berhasil!", Toast.LENGTH_SHORT).show()
                handleConfirmation()
            }
        }

        togglePaymentMethod.addOnButtonCheckedListener { _, checkedId, isChecked ->
            if (isChecked) {
                when (checkedId) {
                    R.id.btn_tunai -> {
                        selectedMethod = "Tunai"
                        llCashContainer.visibility = View.VISIBLE
                        llQrisContainer.visibility = View.GONE
                        llEwalletContainer.visibility = View.GONE
                    }
                    R.id.btn_qris -> {
                        selectedMethod = "QRIS"
                        llCashContainer.visibility = View.GONE
                        llQrisContainer.visibility = View.VISIBLE
                        llEwalletContainer.visibility = View.GONE
                        etCashReceived.text = null
                    }
                    R.id.btn_transfer -> {
                        selectedMethod = "E-Wallet"
                        llCashContainer.visibility = View.GONE
                        llQrisContainer.visibility = View.GONE
                        llEwalletContainer.visibility = View.VISIBLE
                        etCashReceived.text = null
                    }
                }
            }
        }
    }

    private fun setupCashCalculator(order: ModelOrder) {
        val totalAkhir = order.totalHarga ?: 0

        etCashReceived.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val input = s.toString().toIntOrNull() ?: 0
                val kembalian = input - totalAkhir
                if (kembalian >= 0) {
                    tvChangeAmount.text = formatRupiah(kembalian)
                } else {
                    tvChangeAmount.text = "Rp 0"
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        // Chips listeners
        chipExactAmount.setOnClickListener {
            etCashReceived.setText(totalAkhir.toString())
        }
        chip50k.setOnClickListener {
            etCashReceived.setText("50000")
        }
        chip100k.setOnClickListener {
            etCashReceived.setText("100000")
        }
    }

    private fun handleConfirmation() {
        val order = currentOrder ?: return
        val totalAkhir = order.totalHarga ?: 0
        var cash = 0
        var change = 0

        if (selectedMethod == "Tunai") {
            val inputCash = etCashReceived.text.toString().trim()
            if (inputCash.isEmpty()) {
                etCashReceived.error = "Jumlah uang wajib diisi"
                return
            }
            cash = inputCash.toIntOrNull() ?: 0
            if (cash < totalAkhir) {
                etCashReceived.error = "Uang yang diterima kurang dari total tagihan!"
                return
            }
            change = cash - totalAkhir
        }

        order.metodeBayar = selectedMethod
        order.uangDiterima = if (selectedMethod == "Tunai") cash else totalAkhir
        order.kembalian = if (selectedMethod == "Tunai") change else 0
        order.status = "PAID"

        // Hitung Keuntungan (Profit)
        val totalKeuntungan = order.items?.sumOf { item ->
            val jual = item.hargaJual ?: 0
            val beli = item.hargaBeli ?: 0
            val qty = item.qty ?: 0
            (jual - beli) * qty
        } ?: 0
        order.keuntungan = totalKeuntungan

        val database = FirebaseDatabase.getInstance()
        
        // 1. Simpan Transaksi Ke Firebase
        val orderRef = database.getReference("orders").child(order.idOrder ?: "ORD-${System.currentTimeMillis()}")
        orderRef.setValue(order).addOnSuccessListener {
            
            // 2. Kurangi Stok Produk Secara Aman di Firebase (menggunakan Transaction)
            order.items?.forEach { item ->
                val idProduk = item.idProduk
                val qty = item.qty ?: 0
                val isUnlimited = item.tanpaBatas == "ya"

                if (!idProduk.isNullOrEmpty() && !isUnlimited) {
                    val stockRef = database.getReference("produk").child(idProduk).child("stokProduk")
                    stockRef.runTransaction(object : Transaction.Handler {
                        override fun doTransaction(mutableData: MutableData): Transaction.Result {
                            val currentStock = mutableData.getValue(Int::class.java) ?: 0
                            val newStock = if (currentStock >= qty) currentStock - qty else 0
                            mutableData.value = newStock

                            // Tambahkan Notif Stok Rendah jika stok < 5
                            if (newStock < 5) {
                                val notifRef = database.getReference("notifications").push()
                                val stockNotif = ModelNotification(
                                    id = notifRef.key,
                                    type = "stok",
                                    title = "Stok Hampir Habis!",
                                    message = "Produk ${item.namaProduk} sisa $newStock. Segera tambah stok!",
                                    timestamp = System.currentTimeMillis(),
                                    isRead = false,
                                    targetId = idProduk
                                )
                                notifRef.setValue(stockNotif)
                            }

                            return Transaction.success(mutableData)
                        }

                        override fun onComplete(
                            error: DatabaseError?,
                            committed: Boolean,
                            currentData: DataSnapshot?
                        ) {}
                    })
                }
            }

            // 3. Tambah Notifikasi Transaksi Berhasil
            val notifRef = database.getReference("notifications").push()
            val transNotif = ModelNotification(
                id = notifRef.key,
                type = "transaksi",
                title = "Pembayaran Berhasil",
                message = "Transaksi ${order.idOrder} senilai ${formatRupiah(totalAkhir)} telah sukses.",
                timestamp = System.currentTimeMillis(),
                isRead = false,
                targetId = order.idOrder
            )
            notifRef.setValue(transNotif)

            // 4. Tambah Catatan Histori
            val historiRef = database.getReference("histori").push()
            val formatWaktu = java.text.SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            val formattedTime = formatWaktu.format(java.util.Date())
            
            val historiData = mapOf(
                "idHistori" to historiRef.key,
                "judul" to "Transaksi Berhasil",
                "deskripsi" to "Order ${order.idOrder} oleh Kasir ${order.namaKasir ?: "-"} senilai ${formatRupiah(totalAkhir)} lunas.",
                "tipe" to "transaksi",
                "timestamp" to System.currentTimeMillis(),
                "tanggalWaktu" to formattedTime
            )
            historiRef.setValue(historiData)

            // 4. Tambah saldo toko otomatis sesuai total pembayaran
            SaldoManager.tambahSaldo(totalAkhir) { saldoOk ->
                runOnUiThread {
                    if (!saldoOk) {
                        Toast.makeText(
                            this,
                            "Transaksi tersimpan, namun saldo gagal diperbarui",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    Toast.makeText(this, "Pembayaran Berhasil!", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, ReceiptActivity::class.java).apply {
                        putExtra("ORDER_DATA", order)
                    }
                    startActivity(intent)
                    finish()
                }
            }

        }.addOnFailureListener {
            Toast.makeText(this, "Gagal memproses transaksi: ${it.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }
}
