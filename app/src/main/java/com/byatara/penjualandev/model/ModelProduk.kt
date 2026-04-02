package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

class ModelProduk(
    var idProduk: String? = null,
    var namaProduk: String? = null,
    var fotoProduk: String? = null,
    var deskripsiProduk: String? = null,
    var idKategori: String? = null,
    var idCabang: String? = null,
    var stokProduk: Int? = 0,
    var tanpaBatas: String? = null,
    var hargaBeli: Int? = 0,
    var hargaJual: Int? = 0,
    var tipeKeuntungan: String? = null,
    var manajemenStok: String? = null,
    var statusProduk: String? = null,
    var createdAt: String? = null,
    var updatedAt: String? = null
) : Parcelable {

    var jumlahTerjual: Int = 0

    // Constructor untuk membaca data dari Parcel
    constructor(parcel: Parcel) : this(
        idProduk = parcel.readString(),
        namaProduk = parcel.readString(),
        fotoProduk = parcel.readString(),
        deskripsiProduk = parcel.readString(),
        idKategori = parcel.readString(),
        idCabang = parcel.readString(),
        stokProduk = parcel.readValue(Int::class.java.classLoader) as? Int,
        tanpaBatas = parcel.readString(),
        hargaBeli = parcel.readValue(Int::class.java.classLoader) as? Int,
        hargaJual = parcel.readValue(Int::class.java.classLoader) as? Int,
        tipeKeuntungan = parcel.readString(),
        manajemenStok = parcel.readString(),
        statusProduk = parcel.readString(),
        createdAt = parcel.readString(),
        updatedAt = parcel.readString()
    ) {
        jumlahTerjual = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idProduk)
        parcel.writeString(namaProduk)
        parcel.writeString(fotoProduk)
        parcel.writeString(deskripsiProduk)
        parcel.writeString(idKategori)
        parcel.writeString(idCabang)
        parcel.writeValue(stokProduk)
        parcel.writeString(tanpaBatas)
        parcel.writeValue(hargaBeli)
        parcel.writeValue(hargaJual)
        parcel.writeString(tipeKeuntungan)
        parcel.writeString(manajemenStok)
        parcel.writeString(statusProduk)
        parcel.writeString(createdAt)
        parcel.writeString(updatedAt)
        parcel.writeInt(jumlahTerjual)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelProduk> {
        override fun createFromParcel(parcel: Parcel): ModelProduk = ModelProduk(parcel)
        override fun newArray(size: Int): Array<ModelProduk?> = arrayOfNulls(size)
    }
}