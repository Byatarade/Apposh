package com.byatara.penjualandev.viewmodel

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.byatara.penjualandev.model.ModelCabang
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class CabangViewModel : ViewModel() {
    private val database = FirebaseDatabase.getInstance()
    private val myRef = database.getReference("cabang")

    val cabangList = MutableLiveData<List<ModelCabang>>()
    private var originalCabangList = ArrayList<ModelCabang>()

    val isLoading = MutableLiveData<Boolean>()
    val isSearchEmpty = MutableLiveData<Boolean>()

    init {
        getData()
    }

    fun getData() {
        isLoading.value = true
        val query = myRef.orderByChild("idCabang").limitToLast(100)
        query.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                isLoading.value = false
                if (snapshot.exists()) {
                    val list = ArrayList<ModelCabang>()
                    for (dataSnapshot in snapshot.children) {
                        val cabang = dataSnapshot.getValue(ModelCabang::class.java)
                        if (cabang == null) {
                            Log.e("CabangViewModel", "Failed to read value")
                        } else {
                            list.add(cabang)
                        }
                    }
                    originalCabangList.clear()
                    originalCabangList.addAll(list)
                    cabangList.value = originalCabangList
                    isSearchEmpty.value = list.isEmpty()
                } else {
                    cabangList.value = emptyList()
                    isSearchEmpty.value = true
                }
            }

            override fun onCancelled(error: DatabaseError) {
                isLoading.value = false
                Log.e("CabangViewModel", "onCancelled", error.toException())
            }
        })
    }

    fun filter(query: String) {
        if (query.isEmpty()) {
            cabangList.value = originalCabangList
            isSearchEmpty.value = originalCabangList.isEmpty()
        } else {
            val lowerQuery = query.lowercase()
            val filteredList = originalCabangList.filter { cabang ->
                cabang.namaCabang?.lowercase()?.contains(lowerQuery) == true
            }
            cabangList.value = filteredList
            isSearchEmpty.value = filteredList.isEmpty()
        }
    }
}
