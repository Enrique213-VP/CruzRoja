package com.svape.cruzroja.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.Ignore
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "services")
data class Service(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val serviceName: String,
    val description: String,
    val date: Date,
    val imageUri: String
) : Parcelable {

    @Ignore
    var volunteers: List<Volunteer> = listOf()

    constructor(parcel: Parcel) : this(
        id = parcel.readInt(),
        serviceName = parcel.readString()!!,
        description = parcel.readString()!!,
        date = Date(parcel.readLong()),
        imageUri = parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(id)
        parcel.writeString(serviceName)
        parcel.writeString(description)
        parcel.writeLong(date.time)
        parcel.writeString(imageUri)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<Service> {
        override fun createFromParcel(parcel: Parcel): Service = Service(parcel)
        override fun newArray(size: Int): Array<Service?> = arrayOfNulls(size)
    }
}

