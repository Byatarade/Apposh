package com.byatara.penjualandev.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.byatara.penjualandev.model.ModelPelanggan
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PelangganViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pelanggan")

    val pelangganList = MutableLiveData<List<ModelPelanggan>>()
    private var originalPelangganList = ArrayList<ModelPelanggan>()

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
                    val list = ArrayList<ModelPelanggan>()
                    for (dataSnapshot in snapshot.children) {
                        val pelanggan = dataSnapshot.getValue(ModelPelanggan::class.java)
                        if (pelanggan == null) {
                            Log.e("PelangganViewModel", "Failed to read value")
                        } else {
                            if (pelanggan.idPelanggan.isNullOrEmpty()) {
                                pelanggan.idPelanggan = dataSnapshot.key
                            }
                            list.add(pelanggan)
                        }
                    }
                    originalPelangganList.clear()
                    originalPelangganList.addAll(list)
                    pelangganList.value = originalPelangganList
                    isSearchEmpty.value = list.isEmpty()
                } else {
                    pelangganList.value = emptyList()
                    isSearchEmpty.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("PelangganViewModel", "onCancelled", error.toException())
            }
        })
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            pelangganList.value = originalPelangganList
            isSearchEmpty.value = originalPelangganList.isEmpty()
        } else {
            val lowerQuery = query.lowercase()
            val filteredList = originalPelangganList.filter { pelanggan ->
                pelanggan.namaPelanggan?.lowercase()?.contains(lowerQuery) == true ||
                pelanggan.teleponPelanggan?.lowercase()?.contains(lowerQuery) == true ||
                pelanggan.alamatPelanggan?.lowercase()?.contains(lowerQuery) == true
            }
            pelangganList.value = filteredList
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }
}
