package com.byatara.penjualandev.utils

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.MutableData
import com.google.firebase.database.Transaction
import com.google.firebase.database.ValueEventListener

object SaldoManager {

    private val saldoRef = FirebaseDatabase.getInstance().getReference("toko").child("saldo")

    fun listenSaldo(onSaldo: (Long) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                onSaldo(parseSaldo(snapshot))
            }

            override fun onCancelled(error: DatabaseError) {
                onSaldo(0L)
            }
        }
        saldoRef.addValueEventListener(listener)
        return listener
    }

    fun removeListener(listener: ValueEventListener) {
        saldoRef.removeEventListener(listener)
    }

    fun tambahSaldo(amount: Int, onComplete: (Boolean) -> Unit) {
        if (amount <= 0) {
            onComplete(true)
            return
        }

        saldoRef.runTransaction(object : Transaction.Handler {
            override fun doTransaction(mutableData: MutableData): Transaction.Result {
                val current = parseSaldoValue(mutableData.value)
                mutableData.value = current + amount
                return Transaction.success(mutableData)
            }

            override fun onComplete(
                error: DatabaseError?,
                committed: Boolean,
                currentData: DataSnapshot?
            ) {
                onComplete(committed && error == null)
            }
        })
    }

    private fun parseSaldo(snapshot: DataSnapshot): Long {
        if (!snapshot.exists()) return 0L
        return parseSaldoValue(snapshot.value)
    }

    private fun parseSaldoValue(value: Any?): Long {
        return when (value) {
            is Long -> value
            is Int -> value.toLong()
            is Double -> value.toLong()
            else -> 0L
        }
    }
}
