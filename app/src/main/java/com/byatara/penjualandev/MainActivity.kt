package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.byatara.penjualandev.cabang.DataCabangActivity
import com.byatara.penjualandev.kategori.DataKategoriActivity
import com.byatara.penjualandev.pegawai.DataPegawaiActivity
import com.byatara.penjualandev.pelanggan.DataPelangganActivity
import com.byatara.penjualandev.produk.DataProdukActivity
import com.byatara.penjualandev.util.formatRupiah
import com.byatara.penjualandev.utils.GreetingHelper
import com.byatara.penjualandev.utils.SaldoManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.byatara.penjualandev.model.ModelOrder
import java.util.Calendar
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private val usersRef = FirebaseDatabase.getInstance().getReference("users")

    private lateinit var tvSalam: TextView
    private lateinit var tvGreeting: TextView
    private lateinit var tvNominal: TextView
    private lateinit var tvTanggalBeranda: TextView
    private lateinit var barChart: BarChart

    private var saldoListener: ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        auth = FirebaseAuth.getInstance()
        tvSalam = findViewById(R.id.tvSalam)
        tvGreeting = findViewById(R.id.tvGreeting)
        tvNominal = findViewById(R.id.tvNominal)
        tvTanggalBeranda = findViewById(R.id.tvTanggalBeranda)
        barChart = findViewById(R.id.barChartPenjualan)

        setupGreeting()
        setupTanggalBeranda()
        loadUserName()
        setupNavigation()
        setupBottomNavigation()
    }

    override fun onStart() {
        super.onStart()
        attachSaldoListener()
        loadChartData()
    }

    override fun onStop() {
        super.onStop()
        saldoListener?.let { SaldoManager.removeListener(it) }
        saldoListener = null
    }

    override fun onResume() {
        super.onResume()
        setupGreeting()
        setupTanggalBeranda()
    }

    private fun setupGreeting() {
        val salamRes = GreetingHelper.getGreetingResId()
        val salamStr = getString(salamRes)
        tvSalam.text = getString(R.string.halo_salam, salamStr)
    }

    private fun setupTanggalBeranda() {
        val format = SimpleDateFormat("dd MMMM yyyy", Locale("id", "ID"))
        tvTanggalBeranda.text = format.format(Date())
    }

    private fun loadUserName() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            tvGreeting.text = "User"
            return
        }

        usersRef.child(currentUser.uid).get().addOnSuccessListener { snapshot ->
            tvGreeting.text = if (snapshot.exists()) {
                snapshot.child("name").value?.toString()?.takeIf { it.isNotBlank() }
                    ?: currentUser.email?.substringBefore("@")
                    ?: "User"
            } else {
                currentUser.email?.substringBefore("@") ?: "User"
            }
        }.addOnFailureListener {
            tvGreeting.text = currentUser.email?.substringBefore("@") ?: "User"
        }
    }

    private fun attachSaldoListener() {
        saldoListener?.let { SaldoManager.removeListener(it) }
        saldoListener = SaldoManager.listenSaldo { saldo ->
            tvNominal.text = formatRupiah(saldo)
        }
    }

    private fun setupNavigation() {
        findViewById<androidx.cardview.widget.CardView>(R.id.cardkategori).setOnClickListener {
            startActivity(Intent(this, DataKategoriActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardmenu).setOnClickListener {
            startActivity(Intent(this, DataProdukActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardcabang).setOnClickListener {
            startActivity(Intent(this, DataCabangActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardpegawai).setOnClickListener {
            startActivity(Intent(this, DataPegawaiActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.btn_transaksi).setOnClickListener {
            startActivity(Intent(this, TransaksiActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.btn_pelanggan).setOnClickListener {
            startActivity(Intent(this, DataPelangganActivity::class.java))
        }

        findViewById<android.widget.LinearLayout>(R.id.btn_laporan).setOnClickListener {
            startActivity(Intent(this, LaporanActivity::class.java))
        }

        findViewById<androidx.cardview.widget.CardView>(R.id.cardprinter).setOnClickListener {
            startActivity(Intent(this, PrintHistoryActivity::class.java))
        }
    }

    private fun setupBottomNavigation() {
        com.byatara.penjualandev.utils.BottomNavigationHelper.setup(this, R.id.navigation_home)
    }

    private fun loadChartData() {
        val ordersRef = FirebaseDatabase.getInstance().getReference("orders")
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: com.google.firebase.database.DataSnapshot) {
                val orders = mutableListOf<ModelOrder>()
                for (child in snapshot.children) {
                    val order = child.getValue(ModelOrder::class.java)
                    if (order != null) {
                        orders.add(order)
                    }
                }
                processAndShowChart(orders)
            }
            override fun onCancelled(error: com.google.firebase.database.DatabaseError) {}
        })
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
        val entries = ArrayList<BarEntry>()
        val dateFormat = SimpleDateFormat("dd/MM", Locale("id", "ID"))
        val parseFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))

        for (i in 0..6) {
            val startOfDay = calendar.timeInMillis
            val endOfDay = startOfDay + 24 * 60 * 60 * 1000 - 1
            
            val dailyOrders = orders.filter { order ->
                val ts = order.timestamp ?: try { parseFormat.parse(order.tanggalWaktu ?: "")?.time } catch (e: Exception) { null }
                ts != null && ts in startOfDay..endOfDay
            }
            val dailyTotal = dailyOrders.sumOf { it.totalHarga ?: 0 }
            
            labels.add(dateFormat.format(calendar.time))
            entries.add(BarEntry(i.toFloat(), dailyTotal.toFloat()))
            
            calendar.add(Calendar.DAY_OF_YEAR, 1) // next day
        }

        val dataSet = BarDataSet(entries, "Penjualan (Rp)")
        dataSet.color = ContextCompat.getColor(this, R.color.colorPrimary)
        dataSet.valueTextSize = 10f
        
        val data = BarData(dataSet)
        data.barWidth = 0.5f

        barChart.data = data
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.xAxis.setDrawGridLines(false)
        barChart.xAxis.granularity = 1f
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.animateY(1000)
        barChart.invalidate()
    }
}
