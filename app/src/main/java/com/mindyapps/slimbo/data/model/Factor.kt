package com.mindyapps.slimbo.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "factors")
data class Factor(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String?,
    val resource_name: String?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(resource_name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Factor> {
        override fun createFromParcel(parcel: Parcel): Factor {
            return Factor(parcel)
        }

        override fun newArray(size: Int): Array<Factor?> {
            return arrayOfNulls(size)
        }
    }
}