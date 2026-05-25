package com.byatara.penjualandev.model

data class ModelNotification(
    var id: String? = null,
    val type: String? = "transaksi", // "transaksi", "stok", "info"
    val title: String? = null,
    val message: String? = null,
    val timestamp: Long? = System.currentTimeMillis(),
    val isRead: Boolean? = false,
    val targetId: String? = null // idOrder atau idProduk
)
