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

import androidx.activity.result.contract.ActivityResultContracts
import android.graphics.pdf.PdfDocument
import android.graphics.Paint
import android.graphics.Color
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import java.io.OutputStreamWriter
import androidx.appcompat.app.AlertDialog

class LaporanActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var tvEmptyMessage: TextView
    private lateinit var tvTotalPendapatan: TextView
    private lateinit var tvJumlahTransaksi: TextView
    private lateinit var tvAvgTransaksi: TextView
    private lateinit var searchView: SearchView
    private lateinit var btnFilterTanggal: MaterialButton
    private lateinit var cgPaymentFilter: com.google.android.material.chip.ChipGroup
    private lateinit var cardExport: com.google.android.material.card.MaterialCardView
    private lateinit var barChart: com.github.mikephil.charting.charts.BarChart

    private lateinit var adapter: LaporanTransaksiAdapter
    private val database = FirebaseDatabase.getInstance()
    private val ordersRef = database.getReference("orders")

    private var allOrders = listOf<ModelOrder>()
    private var filteredOrders = listOf<ModelOrder>()
    private var searchQuery = ""
    private var filterDayStartMillis: Long? = null
    private var selectedPaymentMethod = "Semua"

    private val dateDisplayFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
    private val dateTimeParseFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

    private val exportCsvLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("text/csv")) { uri ->
        uri?.let { writeCsvToUri(it) }
    }
    
    private val exportPdfLauncher = registerForActivityResult(ActivityResultContracts.CreateDocument("application/pdf")) { uri ->
        uri?.let { writePdfToUri(it) }
    }

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

        // Setup Bottom Navigation using reusable helper
        com.byatara.penjualandev.utils.BottomNavigationHelper.setup(this, R.id.navigation_analytics)
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_laporan_transaksi)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)
        tvEmptyMessage = findViewById(R.id.tv_empty_message)
        tvTotalPendapatan = findViewById(R.id.tv_total_pendapatan)
        tvJumlahTransaksi = findViewById(R.id.tv_jumlah_transaksi)
        tvAvgTransaksi = findViewById(R.id.tv_avg_transaksi)
        searchView = findViewById(R.id.search_laporan)
        btnFilterTanggal = findViewById(R.id.btn_filter_tanggal)
        cgPaymentFilter = findViewById(R.id.cg_payment_filter)
        cardExport = findViewById(R.id.card_export)
        barChart = findViewById(R.id.barChartPenjualan)

        cardExport.setOnClickListener {
            showExportDialog()
        }

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
        filteredOrders = filtered
        
        processAndShowChart(filtered)

        val totalRevenue = filtered.sumOf { it.totalHarga ?: 0 }
        val avgTransaction = if (filtered.isNotEmpty()) totalRevenue / filtered.size else 0

        tvTotalPendapatan.text = formatRupiah(totalRevenue)
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

    private fun processAndShowChart(orders: List<ModelOrder>) {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)
        
        // Go back 6 days (to include today = 7 days)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        
        val labels = ArrayList<String>()
        val entries = ArrayList<com.github.mikephil.charting.data.BarEntry>()
        val dateFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))

        for (i in 0..6) {
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
            
            val dailyOrders = orders.filter { order ->
                val ts = order.timestamp ?: parseOrderDate(order.tanggalWaktu)
                ts != null && ts in startOfDay..endOfDay
            }
            val dailyTotal = dailyOrders.sumOf { it.totalHarga ?: 0 }
            
            labels.add(dateFormat.format(calendar.time))
            entries.add(com.github.mikephil.charting.data.BarEntry(i.toFloat(), dailyTotal.toFloat()))
            
            calendar.add(Calendar.DAY_OF_YEAR, 1) // next day
        }

        val dataSet = com.github.mikephil.charting.data.BarDataSet(entries, "Penjualan (Rp)")
        dataSet.color = androidx.core.content.ContextCompat.getColor(this, R.color.colorPrimary)
        dataSet.valueTextSize = 10f
        
        // Dapatkan warna teks yang sesuai dengan tema (Terang/Gelap)
        val typedValue = android.util.TypedValue()
        theme.resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true)
        val textColor = typedValue.data
        
        dataSet.valueTextColor = textColor
        
        val data = com.github.mikephil.charting.data.BarData(dataSet)
        data.barWidth = 0.5f

        barChart.data = data
        barChart.xAxis.valueFormatter = com.github.mikephil.charting.formatter.IndexAxisValueFormatter(labels)
        barChart.xAxis.position = com.github.mikephil.charting.components.XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.granularity = 1f
        barChart.xAxis.textColor = textColor
        
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.textColor = textColor
        
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        
        barChart.legend.textColor = textColor
        
        barChart.animateY(1000)
        barChart.invalidate()
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

    private fun showExportDialog() {
        if (filteredOrders.isEmpty()) {
            Toast.makeText(this, "Tidak ada data untuk diekspor", Toast.LENGTH_SHORT).show()
            return
        }
        val options = arrayOf("Ekspor ke CSV (Excel)", "Ekspor ke PDF")
        MaterialAlertDialogBuilder(this)
            .setTitle("Ekspor Laporan")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> exportCsvLauncher.launch("Laporan_Transaksi_${System.currentTimeMillis()}.csv")
                    1 -> exportPdfLauncher.launch("Laporan_Transaksi_${System.currentTimeMillis()}.pdf")
                }
            }
            .show()
    }

    private fun writeCsvToUri(uri: android.net.Uri) {
        try {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Header
                    writer.write("ID Order,Tanggal,Kasir,Pelanggan,Metode,Total Harga\n")
                    // Data
                    for (order in filteredOrders) {
                        val id = order.idOrder ?: "-"
                        val tgl = order.tanggalWaktu ?: "-"
                        val kasir = order.namaKasir ?: "-"
                        val pel = order.namaPelanggan ?: "-"
                        val met = order.metodeBayar ?: "-"
                        val total = order.totalHarga ?: 0
                        writer.write("$id,$tgl,$kasir,$pel,$met,$total\n")
                    }
                }
            }
            Toast.makeText(this, "Berhasil mengekspor CSV", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengekspor CSV: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun writePdfToUri(uri: android.net.Uri) {
        try {
            val pdfDocument = PdfDocument()
            val paint = Paint()
            val titlePaint = Paint()

            // Buat halaman A4
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            var page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas

            titlePaint.textSize = 18f
            titlePaint.isFakeBoldText = true
            titlePaint.color = Color.BLACK
            
            paint.textSize = 12f
            paint.color = Color.BLACK

            var yPosition = 50f
            canvas.drawText("Laporan Transaksi Apposh", 50f, yPosition, titlePaint)
            yPosition += 40f

            canvas.drawText("ID Order", 50f, yPosition, titlePaint)
            canvas.drawText("Tanggal", 150f, yPosition, titlePaint)
            canvas.drawText("Total", 450f, yPosition, titlePaint)
            
            yPosition += 20f

            for (order in filteredOrders) {
                if (yPosition > 800f) {
                    pdfDocument.finishPage(page)
                    page = pdfDocument.startPage(pageInfo)
                    canvas = page.canvas
                    yPosition = 50f
                }
                val id = order.idOrder ?: "-"
                val tgl = order.tanggalWaktu ?: "-"
                val total = formatRupiah(order.totalHarga ?: 0)
                
                val shortId = if (id.length > 12) id.substring(0, 12) + "..." else id
                val shortTgl = if (tgl.length > 15) tgl.substring(0, 15) else tgl

                canvas.drawText(shortId, 50f, yPosition, paint)
                canvas.drawText(shortTgl, 150f, yPosition, paint)
                canvas.drawText(total, 450f, yPosition, paint)
                
                yPosition += 20f
            }

            pdfDocument.finishPage(page)

            contentResolver.openOutputStream(uri)?.use { outputStream ->
                pdfDocument.writeTo(outputStream)
            }
            pdfDocument.close()
            Toast.makeText(this, "Berhasil mengekspor PDF", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Gagal mengekspor PDF: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val DAY_MILLIS = 24L * 60L * 60L * 1000L
    }
}
