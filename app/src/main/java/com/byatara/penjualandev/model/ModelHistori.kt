package com.byatara.penjualandev.model

data class ModelHistori(
    var idHistori: String? = null,
    var judul: String? = null,
    var deskripsi: String? = null,
    var tipe: String? = null, // e.g., "produk", "kategori", "transaksi", "pegawai"
    var timestamp: Long? = 0L,
    var tanggalWaktu: String? = null
)
