package com.byatara.penjualandev.model

import android.os.Parcel
import android.os.Parcelable

data class ModelCabang(
    var idCabang: String? = null,
    var namaCabang: String? = null,
    var alamatCabang: String? = null,
    var teleponCabang: String? = null,
    var statusCabang: Boolean? = null
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(idCabang)
        parcel.writeString(namaCabang)
        parcel.writeString(alamatCabang)
        parcel.writeString(teleponCabang)
        parcel.writeByte(if (statusCabang == true) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<ModelCabang> {
        override fun createFromParcel(parcel: Parcel): ModelCabang {
            return ModelCabang(parcel)
        }

        override fun newArray(size: Int): Array<ModelCabang?> {
            return arrayOfNulls(size)
        }
    }
}
