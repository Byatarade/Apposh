package com.byatara.penjualandev.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.byatara.penjualandev.model.ModelProduk
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProdukViewModel : ViewModel() {

    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("produk")

    // LiveData list produk yang akan di-observe oleh Activity/Fragment
    val produkList = MutableLiveData<List<ModelProduk>>()

    // Menyimpan list asli untuk keperluan filtering
    private var originalProdukList = ArrayList<ModelProduk>()

    // LiveData status loading & pencarian kosong
    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    /**
     * Mengambil semua data produk dari Firebase Realtime Database.
     * Data di-map ke ModelProduk dan disimpan di produkList.
     */
    fun getData() {
        isLoading.value = true
        val query = myRef.orderByChild("idProduk").limitToLast(200)

        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelProduk>()
                    for (dataSnapshot in snapshot.children) {
                        val produk = dataSnapshot.getValue(ModelProduk::class.java)
                        if (produk == null) {
                            Log.e("ProdukViewModel", "Gagal membaca data produk")
                        } else {
                            list.add(produk)
                        }
                    }
                    originalProdukList.clear()
                    originalProdukList.addAll(list)
                    produkList.value = originalProdukList
                    isSearchEmpty.value = list.isEmpty()
                } else {
                    produkList.value = emptyList()
                    isSearchEmpty.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("ProdukViewModel", "onCancelled: ${error.message}", error.toException())
            }
        })
    }

    /**
     * Filter produk berdasarkan nama produk (case-insensitive).
     * Jika query kosong, tampilkan semua produk dari originalProdukList.
     *
     * @param query String pencarian dari SearchView/EditText
     */
    fun filter(query: String) {
        if (query.isEmpty()) {
            produkList.value = originalProdukList
            isSearchEmpty.value = originalProdukList.isEmpty()
        } else {
            val lowerQuery = query.lowercase()
            val filteredList = originalProdukList.filter { produk ->
                produk.namaProduk?.lowercase()?.contains(lowerQuery) == true
            }
            produkList.value = filteredList
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }

    /**
     * Filter produk berdasarkan idKategori.
     * Berguna untuk menampilkan produk per kategori.
     *
     * @param idKategori ID kategori yang difilter, null untuk tampilkan semua
     */
    fun filterByKategori(idKategori: String?) {
        if (idKategori.isNullOrEmpty()) {
            produkList.value = originalProdukList
        } else {
            val filteredList = originalProdukList.filter { produk ->
                produk.idKategori == idKategori
            }
            produkList.value = filteredList
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }

    /**
     * Filter produk berdasarkan status produk ("aktif" / "nonaktif").
     *
     * @param status String status produk yang difilter
     */
    fun filterByStatus(status: String) {
        val filteredList = originalProdukList.filter { produk ->
            produk.statusProduk?.lowercase() == status.lowercase()
        }
        produkList.value = filteredList
        isSearchEmpty.value = filteredList.isEmpty()
    }
}