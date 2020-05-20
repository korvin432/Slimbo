package com.mindyapps.android.slimbo.data.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.mindyapps.android.slimbo.data.db.ListConverter
import java.util.*

@Entity(tableName = "notes")
@TypeConverters(ListConverter::class)
data class Note(
    @PrimaryKey(autoGenerate = true)
    val id: Int? = null,
    val recordings: List<AudioRecord>,
    val factors: List<Factor>
) : Parcelable {

    constructor(parcel: Parcel) : this(
        parcel.readValue(Int::class.java.classLoader) as? Int,
        listOf<AudioRecord>().apply {
            parcel.readList(this, AudioRecord::class.java.classLoader)
        },
        listOf<Factor>().apply {
            parcel.readList(this, Factor::class.java.classLoader)
        }
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeValue(id)
        parcel.writeList(recordings)
        parcel.writeList(factors)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Note> {
        override fun createFromParcel(parcel: Parcel): Note {
            return Note(parcel)
        }

        override fun newArray(size: Int): Array<Note?> {
            return arrayOfNulls(size)
        }
    }
}