package com.svape.cruzroja.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date


data class Service(
    val serviceName: String,
    val date: Date,
    val volunteers: List<Volunteer>,
    val imageUri: String
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        Date(parcel.readLong()),
        parcel.createTypedArrayList(Volunteer.CREATOR)!!,
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(serviceName)
        parcel.writeLong(date.time)
        parcel.writeTypedList(volunteers)
        parcel.writeString(imageUri)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Service> {
        override fun createFromParcel(parcel: Parcel): Service = Service(parcel)
        override fun newArray(size: Int): Array<Service?> = arrayOfNulls(size)
    }
}