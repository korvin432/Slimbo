package com.mindyapps.android.slimbo.data.db

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.mindyapps.android.slimbo.data.model.AudioRecord
import com.mindyapps.android.slimbo.data.model.Factor
import java.lang.reflect.Type


class ListConverter {

    @TypeConverter
    fun fromAudioRecordList(value: List<AudioRecord>): String {
        val gson = Gson()
        val type = object : TypeToken<List<AudioRecord>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toAudioRecordList(value: String): List<AudioRecord> {
        val gson = Gson()
        val type = object : TypeToken<List<AudioRecord>>() {}.type
        return gson.fromJson(value, type)
    }


    @TypeConverter
    fun fromFactorList(value: List<Factor>): String {
        val gson = Gson()
        val type = object : TypeToken<List<Factor>>() {}.type
        return gson.toJson(value, type)
    }

    @TypeConverter
    fun toFactorList(value: String): List<Factor> {
        val gson = Gson()
        val type = object : TypeToken<List<Factor>>() {}.type
        return gson.fromJson(value, type)
    }
}