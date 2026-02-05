package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

data class ModelKategori(
    var idKategori: String? = null,
    var namaKategori: String? = null,
    var statusKategori: Boolean? = null
) : Parcelable {

    // Constructor for reading from Parcel
    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        // Boolean needs to be read carefully; 1 is true, 0 is false
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idKategori)
        parcel.writeString(namaKategori)
        // Write boolean as a Byte
        parcel.writeByte(if (statusKategori == true) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelKategori> {
        override fun createFromParcel(parcel: Parcel): ModelKategori {
            return ModelKategori(parcel)
        }

        override fun newArray(size: Int): Array<ModelKategori?> {
            return arrayOfNulls(size)
        }
    }
}