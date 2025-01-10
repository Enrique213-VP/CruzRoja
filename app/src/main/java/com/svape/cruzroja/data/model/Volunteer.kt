package com.svape.cruzroja.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "volunteers")
data class Volunteer(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    var serviceId: Int,
    val name: String,
    val hours: Int
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        serviceId = parcel.readInt(),
        name = parcel.readString()!!,
        hours = parcel.readInt()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeInt(serviceId)
        parcel.writeString(name)
        parcel.writeInt(hours)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Volunteer> {
        override fun createFromParcel(parcel: Parcel): Volunteer = Volunteer(parcel)
        override fun newArray(size: Int): Array<Volunteer?> = arrayOfNulls(size)
    }
}