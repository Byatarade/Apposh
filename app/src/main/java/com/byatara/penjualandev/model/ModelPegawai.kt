package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

data class ModelPegawai(
    var idPegawai: String? = null,
    var namaPegawai: String? = null,
    var jabatanPegawai: String? = null,
    var teleponPegawai: String? = null,
    var statusPegawai: Boolean? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idPegawai)
        parcel.writeString(namaPegawai)
        parcel.writeString(jabatanPegawai)
        parcel.writeString(teleponPegawai)
        parcel.writeByte(if (statusPegawai == true) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelPegawai> {
        override fun createFromParcel(parcel: Parcel): ModelPegawai {
            return ModelPegawai(parcel)
        }

        override fun newArray(size: Int): Array<ModelPegawai?> {
            return arrayOfNulls(size)
        }
    }
}
