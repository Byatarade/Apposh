package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

data class ModelOrderItem(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var fotoProduk: String? = null,
    var hargaJual: Int? = 0,
    var qty: Int? = 0,
    var subtotal: Int? = 0,
    var tanpaBatas: String? = null // untuk cek apakah stok dikurangi
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeString(fotoProduk)
        parcel.writeValue(hargaJual)
        parcel.writeValue(qty)
        parcel.writeValue(subtotal)
        parcel.writeString(tanpaBatas)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelOrderItem> {
        override fun createFromParcel(parcel: Parcel): ModelOrderItem = ModelOrderItem(parcel)
        override fun newArray(size: Int): Array<ModelOrderItem?> = arrayOfNulls(size)
    }
}
