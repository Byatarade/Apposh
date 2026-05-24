package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

data class ModelOrder(
    var idOrder: String? = null,
    var namaKasir: String? = null,
    var namaPelanggan: String? = null,
    var nomorMeja: String? = null,
    var catatan: String? = null,
    var items: List<ModelOrderItem>? = null,
    var metodeBayar: String? = null,
    var subtotal: Int? = 0,
    var pajak: Int? = 0,
    var totalHarga: Int? = 0,
    var uangDiterima: Int? = 0,
    var kembalian: Int? = 0,
    var status: String? = "PAID",
    var timestamp: Long? = 0L,
    var tanggalWaktu: String? = null,
    var idCabang: String? = null,
    var keuntungan: Int? = 0
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.createTypedArrayList(ModelOrderItem.CREATOR),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idOrder)
        parcel.writeString(namaKasir)
        parcel.writeString(namaPelanggan)
        parcel.writeString(nomorMeja)
        parcel.writeString(catatan)
        parcel.writeTypedList(items)
        parcel.writeString(metodeBayar)
        parcel.writeValue(subtotal)
        parcel.writeValue(pajak)
        parcel.writeValue(totalHarga)
        parcel.writeValue(uangDiterima)
        parcel.writeValue(kembalian)
        parcel.writeString(status)
        parcel.writeValue(timestamp)
        parcel.writeString(tanggalWaktu)
        parcel.writeString(idCabang)
        parcel.writeValue(keuntungan)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelOrder> {
        override fun createFromParcel(parcel: Parcel): ModelOrder = ModelOrder(parcel)
        override fun newArray(size: Int): Array<ModelOrder?> = arrayOfNulls(size)
    }
}
