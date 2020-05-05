package com.mindyapps.android.slimbo.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

const val TYPE_MUSIC = "music"
const val TYPE_SOUND = "sound"
const val TYPE_ALARM = "alarm"

@Entity(tableName = "music")
class Music(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val name: String?,
    val fileName: String?,
    val type:String?,
    val duration: Long?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readString(),
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(name)
        parcel.writeString(fileName)
        parcel.writeString(type)
        parcel.writeValue(duration)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Music> {
        override fun createFromParcel(parcel: Parcel): Music {
            return Music(parcel)
        }

        override fun newArray(size: Int): Array<Music?> {
            return arrayOfNulls(size)
        }
    }
}