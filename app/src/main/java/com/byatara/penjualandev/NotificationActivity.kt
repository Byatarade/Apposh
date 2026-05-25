package com.byatara.penjualandev

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.NotificationAdapter
import com.byatara.penjualandev.model.ModelNotification
import com.byatara.penjualandev.model.ModelOrder
import com.byatara.penjualandev.utils.BottomNavigationHelper
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.database.*

class NotificationActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var btnClearAll: MaterialButton
    private lateinit var adapter: NotificationAdapter

    private val database = FirebaseDatabase.getInstance()
    private val notificationsRef = database.getReference("notifications")
    private val ordersRef = database.getReference("orders")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_notification)

        val mainView = findViewById<View>(android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(mainView) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        findViewById<MaterialToolbar>(R.id.toolbar).setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        initViews()
        setupRecyclerView()
        loadNotifications()
        setupBottomNavigation()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rv_notifications)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)
        btnClearAll = findViewById(R.id.btn_clear_all)

        btnClearAll.setOnClickListener {
            clearAllNotifications()
        }
    }

    private fun setupRecyclerView() {
        adapter = NotificationAdapter(emptyList())
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        adapter.setOnItemClickListener { notif ->
            markAsRead(notif)
            handleNotificationClick(notif)
        }
    }

    private fun loadNotifications() {
        viewLoading.visibility = View.VISIBLE
        recyclerView.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        notificationsRef.orderByChild("timestamp").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewLoading.visibility = View.GONE
                val list = mutableListOf<ModelNotification>()
                for (child in snapshot.children) {
                    val notif = child.getValue(ModelNotification::class.java)
                    if (notif != null) {
                        notif.id = child.key
                        list.add(notif)
                    }
                }
                
                val sortedList = list.sortedByDescending { it.timestamp ?: 0L }
                adapter.updateData(sortedList)

                if (sortedList.isEmpty()) {
                    layoutEmpty.visibility = View.VISIBLE
                    recyclerView.visibility = View.GONE
                    btnClearAll.visibility = View.GONE
                } else {
                    layoutEmpty.visibility = View.GONE
                    recyclerView.visibility = View.VISIBLE
                    btnClearAll.visibility = View.VISIBLE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                viewLoading.visibility = View.GONE
                Toast.makeText(this@NotificationActivity, "Gagal memuat notifikasi", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun markAsRead(notif: ModelNotification) {
        val id = notif.id ?: return
        notificationsRef.child(id).child("read").setValue(true)
    }

    private fun handleNotificationClick(notif: ModelNotification) {
        when (notif.type) {
            "transaksi" -> {
                val orderId = notif.targetId ?: return
                ordersRef.child(orderId).get().addOnSuccessListener { snapshot ->
                    val order = snapshot.getValue(ModelOrder::class.java)
                    if (order != null) {
                        val intent = Intent(this, ReceiptActivity::class.java).apply {
                            putExtra("ORDER_DATA", order)
                        }
                        startActivity(intent)
                    } else {
                        Toast.makeText(this, "Data transaksi tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            "stok" -> {
                // Bisa diarahkan ke TambahProdukActivity dengan mode edit jika perlu
                Toast.makeText(this, "Silakan periksa stok produk: ${notif.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun clearAllNotifications() {
        notificationsRef.removeValue().addOnSuccessListener {
            Toast.makeText(this, "Semua notifikasi dihapus", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupBottomNavigation() {
        BottomNavigationHelper.setup(this, R.id.navigation_clock)
    }
}
