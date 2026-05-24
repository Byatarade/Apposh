package com.byatara.penjualandev

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.LaporanTransaksiAdapter
import com.byatara.penjualandev.model.ModelOrder
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvTotalKeuntungan: TextView
    private lateinit var tvJumlahTransaksi: TextView

    private lateinit var adapter: LaporanTransaksiAdapter
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

        // Setup window insets for edge-to-edge layout
        val mainView = findViewById<View>(android.R.id.content)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        // Handle back button on toolbar
        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        toolbar?.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        initViews()
        setupRecyclerView()
        loadDataFromFirebase()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_laporan_transaksi)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)
        tvTotalPendapatan = findViewById(R.id.tv_total_pendapatan)
        tvTotalKeuntungan = findViewById(R.id.tv_total_keuntungan)
        tvJumlahTransaksi = findViewById(R.id.tv_jumlah_transaksi)
    }

    private fun setupRecyclerView() {
        adapter = LaporanTransaksiAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadDataFromFirebase() {
        viewLoading.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewLoading.visibility = View.GONE
                
                if (snapshot.exists()) {
                    val orderList = ArrayList<ModelOrder>()
                    var totalRevenue = 0
                    var totalProfit = 0

                    for (child in snapshot.children) {
                        val order = child.getValue(ModelOrder::class.java)
                        if (order != null) {
                            if (order.idOrder.isNullOrEmpty()) {
                                order.idOrder = child.key
                            }
                            orderList.add(order)
                            totalRevenue += order.totalHarga ?: 0
                            totalProfit += order.keuntungan ?: 0
                        }
                    }

                    // Sort orders by timestamp descending (newest first)
                    orderList.sortByDescending { it.timestamp ?: 0L }

                    adapter.updateData(orderList)

                    tvTotalPendapatan.text = formatRupiah(totalRevenue)
                    tvTotalKeuntungan.text = formatRupiah(totalProfit)
                    tvJumlahTransaksi.text = orderList.size.toString()

                    if (orderList.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        recyclerView.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        recyclerView.visibility = View.VISIBLE
                    }
                } else {
                    tvTotalPendapatan.text = "Rp 0"
                    tvTotalKeuntungan.text = "Rp 0"
                    tvJumlahTransaksi.text = "0"
                    layoutEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                viewLoading.visibility = View.GONE
                layoutEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
                Toast.makeText(this@LaporanActivity, "Gagal memuat data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun formatRupiah(amount: Int): String {
        val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
        return format.format(amount).replace(",00", "")
    }
}
