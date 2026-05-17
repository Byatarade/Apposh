package com.byatara.penjualandev.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.byatara.penjualandev.model.ModelPegawai
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class PegawaiViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("pegawai")

    val pegawaiList = MutableLiveData<List<ModelPegawai>>()
    private var originalPegawaiList = ArrayList<ModelPegawai>()

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
                    val list = ArrayList<ModelPegawai>()
                    for (dataSnapshot in snapshot.children) {
                        val pegawai = dataSnapshot.getValue(ModelPegawai::class.java)
                        if (pegawai == null) {
                            Log.e("PegawaiViewModel", "Failed to read value")
                        } else {
                            if (pegawai.idPegawai.isNullOrEmpty()) {
                                pegawai.idPegawai = dataSnapshot.key
                            }
                            list.add(pegawai)
                        }
                    }
                    originalPegawaiList.clear()
                    originalPegawaiList.addAll(list)
                    pegawaiList.value = originalPegawaiList
                    isSearchEmpty.value = list.isEmpty()
                } else {
                    pegawaiList.value = emptyList()
                    isSearchEmpty.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("PegawaiViewModel", "onCancelled", error.toException())
            }
        })
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            pegawaiList.value = originalPegawaiList
            isSearchEmpty.value = originalPegawaiList.isEmpty()
        } else {
            val lowerQuery = query.lowercase()
            val filteredList = originalPegawaiList.filter { pegawai ->
                pegawai.namaPegawai?.lowercase()?.contains(lowerQuery) == true ||
                pegawai.jabatanPegawai?.lowercase()?.contains(lowerQuery) == true
            }
            pegawaiList.value = filteredList
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }
}
