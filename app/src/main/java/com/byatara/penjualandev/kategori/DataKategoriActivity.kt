package com.byatara.penjualandev.kategori

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.SearchView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.byatara.penjualandev.R
import com.byatara.penjualandev.adapter.KategoriAdapter
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.database.*

class DataKategoriActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var fabTambah: FloatingActionButton
    private lateinit var btnBack: ImageButton
    private lateinit var searchView: SearchView
    private lateinit var adapter: KategoriAdapter
    private val kategoriList = mutableListOf<Kategori>()

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_data_kategori)
        val mainView = findViewById<android.view.View>(R.id.main)
        mainView?.let {
            ViewCompat.setOnApplyWindowInsetsListener(it) { v, insets ->
                val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
                v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
                insets
            }
        }

        initViews()
        setupRecyclerView()
        setupListeners()
        setupSearchView()
        loadKategoriFromFirebase()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.rvDATA_KATEGORI)
        fabTambah = findViewById(R.id.fabDATA_KATEGORI_Tambah)
        btnBack = findViewById(R.id.btn_back)
        searchView = findViewById(R.id.searchView)
    }

    private fun setupRecyclerView() {
        adapter = KategoriAdapter(kategoriList) { kategori ->
            // Navigate to ModKategoriActivity when item is clicked (for edit)
            val intent = Intent(this, ModKategoriActivity::class.java)
            intent.putExtra("KATEGORI_ID", kategori.firebaseKey)
            intent.putExtra("KATEGORI_NAME", kategori.name)
            intent.putExtra("IS_EDIT", true)
            startActivity(intent)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter
    }

    /**
     * Setup SearchView for real-time filtering.
     * Data will be filtered as the user types — no need to press Enter.
     */
    private fun setupSearchView() {
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // Also filter on submit (when user presses Enter)
                adapter.filter(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // Filter in real-time as the user types
                adapter.filter(newText.orEmpty())
                return true
            }
        })
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
                            firebaseKey = kategoriSnapshot.key ?: "",
                            name = name,
                            isActive = isActive
                        )
                        kategoriList.add(kategori)
                    }
                }

                // Store the full list in the adapter for filtering
                adapter.updateFullList(kategoriList.toList())

                // Re-apply current search filter (if any)
                val currentQuery = searchView.query?.toString().orEmpty()
                if (currentQuery.isNotEmpty()) {
                    adapter.filter(currentQuery)
                } else {
                    adapter.notifyDataSetChanged()
                }
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
