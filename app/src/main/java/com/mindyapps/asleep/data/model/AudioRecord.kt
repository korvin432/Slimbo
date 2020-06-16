package com.mindyapps.asleep.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "audio_records")
data class AudioRecord(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val file_name: String?,
    val duration: Long?,
    val creation_date: Long?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readString(),
        parcel.readValue(Long::class.java.classLoader) as? Long,
        parcel.readValue(Long::class.java.classLoader) as? Long
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeString(file_name)
        parcel.writeValue(duration)
        parcel.writeValue(creation_date)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<AudioRecord> {
        override fun createFromParcel(parcel: Parcel): AudioRecord {
            return AudioRecord(parcel)
        }

        override fun newArray(size: Int): Array<AudioRecord?> {
            return arrayOfNulls(size)
        }
    }
}