package com.mindyapps.slimbo.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mindyapps.slimbo.data.db.ListConverter

@Entity(tableName = "recordings")
@TypeConverters(ListConverter::class)
data class Recording(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    val recordings: MutableList<AudioRecord>?,
    val factors: List<Factor>?,
    val rating: Int?,
    val duration: Long?,
    val wake_up_time: Long?,
    val sleep_at_time: Long?
) : Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        mutableListOf<AudioRecord>().apply {
            parcel.readList(this as List<AudioRecord>, AudioRecord::class.java.classLoader)
        },
        listOf<Factor>().apply {
            parcel.readList(this, Factor::class.java.classLoader)
        },
        parcel.readValue(Int::class.java.classLoader) as? Int,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readLong()
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeList(recordings as List<AudioRecord>?)
        parcel.writeList(factors)
        parcel.writeValue(rating)
        parcel.writeLong(duration!!)
        parcel.writeLong(wake_up_time!!)
        parcel.writeLong(sleep_at_time!!)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Recording> {
        override fun createFromParcel(parcel: Parcel): Recording {
            return Recording(parcel)
        }

        override fun newArray(size: Int): Array<Recording?> {
            return arrayOfNulls(size)
        }
    }
}