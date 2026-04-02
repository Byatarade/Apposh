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

    fun getData(){
    isLoading.value=true
        val query = myRef.orderByChild("idKategori").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelKategori>()
                    for(dataSnapshot in snapshot.children){
                        val kategori = dataSnapshot.getValue(ModelKategori::class.java)
                        if (kategori==null){
                            Log.e("DataKategoriViewModel", "Failed to read value")
                        }else{
                            list.add(kategori)
                        }
                    }
                    originalKategoriList.clear()
                    originalKategoriList.addAll(list)
                    kategoriList.value = originalKategoriList
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("DataKategoriViewModel", "onCancelled", error.toException())
            }
        })
    }
}