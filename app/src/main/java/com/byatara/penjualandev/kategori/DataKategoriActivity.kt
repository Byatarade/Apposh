package com.byatara.penjualandev.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var adapter: KategoriAdapter
    private val kategoriList = mutableListOf<Kategori>()

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
        setupRecyclerView()
        setupListeners()
        loadKategoriFromFirebase()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvDATA_KATEGORI)
        fabTambah = findViewById(R.id.fabDATA_KATEGORI_Tambah)
        btnBack = findViewById(R.id.btn_back)
    }

    private fun setupRecyclerView() {
        adapter = KategoriAdapter(kategoriList) { kategori ->
            // Navigate to ModKategoriActivity when item is clicked (for edit)
            val intent = Intent(this, ModKategoriActivity::class.java)
            intent.putExtra("KATEGORI_ID", kategori.id)
            intent.putExtra("KATEGORI_NAME", kategori.name)
            intent.putExtra("IS_EDIT", true)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    private fun loadKategoriFromFirebase() {
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                kategoriList.clear()
                
                for (kategoriSnapshot in snapshot.children) {
                    val id = kategoriSnapshot.child("id").getValue(String::class.java)
                    val name = kategoriSnapshot.child("name").getValue(String::class.java)
                    val isActive = kategoriSnapshot.child("isActive").getValue(Boolean::class.java)
                    
                    // Only add to list if all data is valid (not null)
                    if (!id.isNullOrEmpty() && !name.isNullOrEmpty() && isActive != null) {
                        val kategori = Kategori(
                            id = kategoriList.size + 1,
                            name = name,
                            isActive = isActive
                        )
                        kategoriList.add(kategori)
                    }
                }
                
                adapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle error
            }
        })
    }

    private fun setupListeners() {
        // Navigate to ModKategoriActivity when FAB is clicked
        fabTambah.setOnClickListener {
            startActivity(Intent(this, ModKategoriActivity::class.java))
        }

        // Back button
        btnBack.setOnClickListener {
            finish()
        }
    }
}
