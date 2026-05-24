package com.byatara.penjualandev

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.PrintHistoryAdapter
import com.byatara.penjualandev.model.ModelOrder
import com.byatara.penjualandev.utils.BluetoothPermissionHelper
import com.byatara.penjualandev.utils.BluetoothPrinterHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PrintHistoryActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var adapter: PrintHistoryAdapter

    private val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
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
        setContentView(R.layout.activity_print_history)

        val mainView = findViewById<View>(R.id.main)
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

        recyclerView = findViewById(R.id.rv_print_history)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)

        adapter = PrintHistoryAdapter(emptyList()) { order ->
            confirmAndPrint(order)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        loadOrders()
    }

    private fun loadOrders() {
        viewLoading.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewLoading.visibility = View.GONE

                if (!snapshot.exists()) {
                    showEmpty()
                    return
                }

                val orderList = ArrayList<ModelOrder>()
                for (child in snapshot.children) {
                    val order = child.getValue(ModelOrder::class.java) ?: continue
                    if (order.idOrder.isNullOrEmpty()) {
                        order.idOrder = child.key
                    }
                    if (order.status.equals("PAID", ignoreCase = true)) {
                        orderList.add(order)
                    }
                }

                orderList.sortByDescending { it.timestamp ?: 0L }

                if (orderList.isEmpty()) {
                    showEmpty()
                } else {
                    layoutEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    adapter.updateData(orderList)
                }
            }

            override fun onCancelled(error: DatabaseError) {
                viewLoading.visibility = View.GONE
                showEmpty()
                Toast.makeText(
                    this@PrintHistoryActivity,
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun showEmpty() {
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.VISIBLE
        adapter.updateData(emptyList())
    }

    private fun confirmAndPrint(order: ModelOrder) {
        AlertDialog.Builder(this)
            .setTitle("Cetak Struk")
            .setMessage("Cetak ulang struk untuk ${order.idOrder}?")
            .setPositiveButton("Cetak") { _, _ -> requestPrint(order) }
            .setNegativeButton("Batal", null)
            .show()
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
        Toast.makeText(this, "Menghubungkan ke printer...", Toast.LENGTH_SHORT).show()
        BluetoothPrinterHelper.printReceipt(order) { success, message ->
            Toast.makeText(this, message, if (success) Toast.LENGTH_SHORT else Toast.LENGTH_LONG).show()
        }
    }
}
