package com.byatara.penjualandev.utils

import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.database.FirebaseDatabase

object CrudDeleteHelper {

    fun confirmAndDelete(
        activity: AppCompatActivity,
        title: String,
        message: String,
        firebasePath: String,
        itemId: String,
        historiJudul: String,
        historiDeskripsi: String,
        historiTipe: String,
        onSuccess: () -> Unit
    ) {
        MaterialAlertDialogBuilder(activity)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Hapus") { _, _ ->
                FirebaseDatabase.getInstance()
                    .getReference(firebasePath)
                    .child(itemId)
                    .removeValue()
                    .addOnSuccessListener {
                        CatatanHistori.catat(historiJudul, historiDeskripsi, historiTipe)
                        Toast.makeText(activity, "Berhasil dihapus", Toast.LENGTH_SHORT).show()
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            activity,
                            "Gagal menghapus: ${e.message}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
            .setNegativeButton("Batal", null)
            .show()
    }
}
