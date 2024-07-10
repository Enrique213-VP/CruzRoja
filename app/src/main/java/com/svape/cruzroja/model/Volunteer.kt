package com.svape.cruzroja.model

import android.os.Parcel
import android.os.Parcelable

data class Volunteer(
    val name: String,
    val hours: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(name)
        parcel.writeInt(hours)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Volunteer> {
        override fun createFromParcel(parcel: Parcel): Volunteer = Volunteer(parcel)
        override fun newArray(size: Int): Array<Volunteer?> = arrayOfNulls(size)
    }
}