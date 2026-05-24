package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

data class ModelPelanggan(
    var idPelanggan: String? = null,
    var namaPelanggan: String? = null,
    var teleponPelanggan: String? = null,
    var alamatPelanggan: String? = null,
    var statusPelanggan: Boolean? = true,
    var createdAt: Long? = 0L
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idPelanggan)
        parcel.writeString(namaPelanggan)
        parcel.writeString(teleponPelanggan)
        parcel.writeString(alamatPelanggan)
        parcel.writeByte(if (statusPelanggan == true) 1 else 0)
        parcel.writeValue(createdAt)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelPelanggan> {
        override fun createFromParcel(parcel: Parcel): ModelPelanggan {
            return ModelPelanggan(parcel)
        }

        override fun newArray(size: Int): Array<ModelPelanggan?> {
            return arrayOfNulls(size)
        }
    }
}
