package com.byatara.penjualandev.utils

import com.byatara.penjualandev.model.ModelHistori
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CatatanHistori {

    /**
     * Memasukkan log histori baru ke dalam Firebase Database.
     * @param judul Judul aktivitas (misal: "Produk Baru", "Kategori Dihapus")
     * @param deskripsi Penjelasan detail aktivitas
     * @param tipe Tipe modul aktivitas (misal: "produk", "kategori", "transaksi")
     */
    fun catat(judul: String, deskripsi: String, tipe: String) {
        val database = FirebaseDatabase.getInstance()
        val ref = database.getReference("histori")
        val idHistori = ref.push().key ?: return

        val timestamp = System.currentTimeMillis()
        val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
        val tanggalWaktu = sdf.format(Date(timestamp))

        val histori = ModelHistori(
            idHistori = idHistori,
            judul = judul,
            deskripsi = deskripsi,
            tipe = tipe,
            timestamp = timestamp,
            tanggalWaktu = tanggalWaktu
        )

        ref.child(idHistori).setValue(histori)
    }
}
