package com.byatara.penjualandev.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.byatara.penjualandev.model.ModelKategori
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class DataKategoriViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("kategori")

    val kategoriList = MutableLiveData<List<ModelKategori>>()
    private var originalKategoriList = ArrayList<ModelKategori>()

    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelKategori>()
                    for (dataSnapshot in snapshot.children) {
                        val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                        if (kategori == null) {
                            Log.e("DataKategoriViewModel", "Failed to read value")
                        } else {
                            if (kategori.idKategori.isNullOrEmpty()) {
                                kategori.idKategori = dataSnapshot.key
                            }
                            list.add(kategori)
                        }
                    }
                    originalKategoriList.clear()
                    originalKategoriList.addAll(list)
                    kategoriList.value = originalKategoriList
                    isSearchEmpty.value = list.isEmpty()
                } else {
                    kategoriList.value = emptyList()
                    isSearchEmpty.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataKategoriViewModel", "onCancelled", error.toException())
            }
        })
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            kategoriList.value = originalKategoriList
            isSearchEmpty.value = originalKategoriList.isEmpty()
        } else {
            val lowerQuery = query.lowercase()
            val filteredList = originalKategoriList.filter { kategori ->
                kategori.namaKategori?.lowercase()?.contains(lowerQuery) == true
            }
            kategoriList.value = filteredList
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }
}