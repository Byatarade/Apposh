package com.byatara.penjualandev

import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.adapter.HistoriAdapter
import com.byatara.penjualandev.model.ModelHistori
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.Collections

class HistoriActivity : AppCompatActivity() {

    private lateinit var rvHistori: RecyclerView
    private lateinit var viewLoading: ProgressBar
    private lateinit var layoutEmpty: LinearLayout
    private lateinit var adapter: HistoriAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_histori)

        val mainView = findViewById<View>(R.id.main)
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

        rvHistori = findViewById(R.id.rv_histori)
        viewLoading = findViewById(R.id.view_loading)
        layoutEmpty = findViewById(R.id.layout_empty)

        adapter = HistoriAdapter(mutableListOf())
        rvHistori.layoutManager = LinearLayoutManager(this)
        rvHistori.adapter = adapter

        // Setup Bottom Navigation using reusable helper
        com.byatara.penjualandev.utils.BottomNavigationHelper.setup(this, R.id.navigation_analytics)

        fetchHistori()
    }

    private fun fetchHistori() {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("histori")

        // Tampilkan loading
        viewLoading.visibility = View.VISIBLE
        rvHistori.visibility = View.GONE
        layoutEmpty.visibility = View.GONE

        // Ambil data terbaru (diurutkan berdasarkan key yang merupakan push ID dan selalu berurutan)
        ref.orderByKey().limitToLast(100).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                viewLoading.visibility = View.GONE
                
                if (snapshot.exists()) {
                    val list = mutableListOf<ModelHistori>()
                    for (dataSnapshot in snapshot.children) {
                        val histori = dataSnapshot.getValue(ModelHistori::class.java)
                        if (histori != null) {
                            list.add(histori)
                        }
                    }
                    
                    // Balik list agar yang terbaru di atas
                    list.reverse()
                    
                    if (list.isEmpty()) {
                        layoutEmpty.visibility = View.VISIBLE
                        rvHistori.visibility = View.GONE
                    } else {
                        layoutEmpty.visibility = View.GONE
                        rvHistori.visibility = View.VISIBLE
                        adapter.updateData(list)
                    }
                } else {
                    layoutEmpty.visibility = View.VISIBLE
                    rvHistori.visibility = View.GONE
                }
            }

            override fun onCancelled(error: DatabaseError) {
                viewLoading.visibility = View.GONE
            }
        })
    }
}
