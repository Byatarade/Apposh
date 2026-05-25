package com.byatara.penjualandev

import android.app.DatePickerDialog
import android.os.Bundle
import androidx.appcompat.view.ContextThemeWrapper
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.adapter.LaporanTransaksiAdapter
import com.byatara.penjualandev.model.ModelOrder
import com.byatara.penjualandev.util.applyCleanSearchStyle
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class LaporanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvTotalKeuntungan: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tvAvgTransaksi: TextView
    private lateinit var searchView: SearchView
    private lateinit var btnFilterTanggal: MaterialButton
    private lateinit var cgPaymentFilter: com.google.android.material.chip.ChipGroup

    private lateinit var adapter: LaporanTransaksiAdapter
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("orders")

    private var allOrders = listOf<ModelOrder>()
    private var searchQuery = ""
    private var filterDayStartMillis: Long? = null
    private var selectedPaymentMethod = "Semua"

    private val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val dateTimeParseFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_laporan)

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

        initViews()
        setupRecyclerView()
        setupFilters()
        loadDataFromFirebase()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_laporan_transaksi)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)
        tvEmptyMessage = findViewById(R.id.tv_empty_message)
        tvTotalPendapatan = findViewById(R.id.tv_total_pendapatan)
        tvTotalKeuntungan = findViewById(R.id.tv_total_keuntungan)
        tvJumlahTransaksi = findViewById(R.id.tv_jumlah_transaksi)
        tvAvgTransaksi = findViewById(R.id.tv_avg_transaksi)
        searchView = findViewById(R.id.search_laporan)
        btnFilterTanggal = findViewById(R.id.btn_filter_tanggal)
        cgPaymentFilter = findViewById(R.id.cg_payment_filter)

        // Style all chips to match POS style
        for (i in 0 until cgPaymentFilter.childCount) {
            val chip = cgPaymentFilter.getChildAt(i) as? com.google.android.material.chip.Chip
            chip?.let { styleChip(it) }
        }
    }

    private fun setupRecyclerView() {
        adapter = LaporanTransaksiAdapter(mutableListOf())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun setupFilters() {
        searchView.applyCleanSearchStyle()
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                searchQuery = query.orEmpty().trim()
                applyFilters()
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                searchQuery = newText.orEmpty().trim()
                applyFilters()
                return true
            }
        })

        cgPaymentFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                val chip = group.findViewById<com.google.android.material.chip.Chip>(checkedIds[0])
                selectedPaymentMethod = chip.text.toString()
                applyFilters()
            }
        }

        btnFilterTanggal.setOnClickListener {
            showDatePicker()
        }

        btnFilterTanggal.setOnLongClickListener {
            clearDateFilter()
            true
        }
    }

    private fun showDatePicker() {
        val cal = Calendar.getInstance()
        filterDayStartMillis?.let { cal.timeInMillis = it }

        DatePickerDialog(
            ContextThemeWrapper(this, R.style.Theme_PenjualanDev_DatePicker),
            { _, year, month, dayOfMonth ->
                val selected = Calendar.getInstance().apply {
                    set(Calendar.YEAR, year)
                    set(Calendar.MONTH, month)
                    set(Calendar.DAY_OF_MONTH, dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }
                filterDayStartMillis = selected.timeInMillis
                btnFilterTanggal.text = dateDisplayFormat.format(selected.time)
                applyFilters()
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun clearDateFilter() {
        filterDayStartMillis = null
        btnFilterTanggal.text = "Semua Tanggal"
        applyFilters()
        Toast.makeText(this, "Filter tanggal direset", Toast.LENGTH_SHORT).show()
    }

    private fun loadDataFromFirebase() {
        viewLoading.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        ordersRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewLoading.visibility = View.GONE

                if (!snapshot.exists()) {
                    allOrders = emptyList()
                    applyFilters()
                    return
                }

                val orderList = ArrayList<ModelOrder>()
                for (child in snapshot.children) {
                    val order = child.getValue(ModelOrder::class.java) ?: continue
                    if (order.idOrder.isNullOrEmpty()) {
                        order.idOrder = child.key
                    }
                    orderList.add(order)
                }

                allOrders = orderList.sortedByDescending { it.timestamp ?: 0L }
                applyFilters()
            }

            override fun onCancelled(error: DatabaseError) {
                viewLoading.visibility = View.GONE
                allOrders = emptyList()
                applyFilters()
                Toast.makeText(
                    this@LaporanActivity,
                    "Gagal memuat data: ${error.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    private fun applyFilters() {
        var filtered = allOrders

        filterDayStartMillis?.let { dayStart ->
            val dayEnd = dayStart + DAY_MILLIS - 1
            filtered = filtered.filter { order ->
                val ts = order.timestamp ?: parseOrderDate(order.tanggalWaktu)
                ts != null && ts in dayStart..dayEnd
            }
        }

        if (selectedPaymentMethod != "Semua") {
            filtered = filtered.filter { order ->
                order.metodeBayar?.equals(selectedPaymentMethod, ignoreCase = true) == true
            }
        }

        if (searchQuery.isNotEmpty()) {
            val q = searchQuery.lowercase(Locale.getDefault())
            filtered = filtered.filter { order ->
                order.idOrder?.lowercase()?.contains(q) == true ||
                    order.namaKasir?.lowercase()?.contains(q) == true ||
                    order.namaPelanggan?.lowercase()?.contains(q) == true ||
                    order.metodeBayar?.lowercase()?.contains(q) == true ||
                    order.tanggalWaktu?.lowercase()?.contains(q) == true
            }
        }

        filtered = filtered.sortedByDescending { it.timestamp ?: parseOrderDate(it.tanggalWaktu) ?: 0L }

        val totalRevenue = filtered.sumOf { it.totalHarga ?: 0 }
        val totalProfit = filtered.sumOf { it.keuntungan ?: 0 }
        val avgTransaction = if (filtered.isNotEmpty()) totalRevenue / filtered.size else 0

        tvTotalPendapatan.text = formatRupiah(totalRevenue)
        tvTotalKeuntungan.text = formatRupiah(totalProfit)
        tvJumlahTransaksi.text = filtered.size.toString()
        tvAvgTransaksi.text = formatRupiah(avgTransaction)

        adapter.updateData(filtered)

        when {
            allOrders.isEmpty() -> {
                tvEmptyMessage.text = "Belum ada transaksi terdaftar"
                layoutEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            filtered.isEmpty() -> {
                tvEmptyMessage.text = "Tidak ada transaksi yang cocok dengan filter"
                layoutEmpty.visibility = View.VISIBLE
                recyclerView.visibility = View.GONE
            }
            else -> {
                layoutEmpty.visibility = View.GONE
                recyclerView.visibility = View.VISIBLE
            }
        }
    }

    private fun styleChip(chip: com.google.android.material.chip.Chip) {
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val backgroundColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary),
            android.graphics.Color.TRANSPARENT
        )
        chip.chipBackgroundColor = android.content.res.ColorStateList(states, backgroundColors)
        val textColors = intArrayOf(
            androidx.core.content.ContextCompat.getColor(this, R.color.white),
            androidx.core.content.ContextCompat.getColor(this, R.color.colorSecondaryText)
        )
        chip.setTextColor(android.content.res.ColorStateList(states, textColors))
        val strokeColors = intArrayOf(
            android.graphics.Color.TRANSPARENT,
            androidx.core.content.ContextCompat.getColor(this, R.color.colorOutlineStroke)
        )
        chip.chipStrokeColor = android.content.res.ColorStateList(states, strokeColors)
        chip.chipStrokeWidth = resources.displayMetrics.density * 1f
    }

    private fun parseOrderDate(tanggalWaktu: String?): Long? {
        if (tanggalWaktu.isNullOrBlank()) return null
        return try {
            dateTimeParseFormat.parse(tanggalWaktu)?.time
        } catch (_: Exception) {
            null
        }
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
