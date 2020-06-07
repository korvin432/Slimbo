package com.mindyapps.slimbo.data.db

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.room.*
import com.mindyapps.slimbo.data.model.*

@Dao
interface SlimboDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllFactors(factors: MutableList<Factor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllMusic(music: MutableList<Music>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertRecording(recording: Recording): Long

    @Query("UPDATE recordings SET rating = :newRating WHERE id = :recId")
    fun updateRecording(recId: Int, newRating: Int)

    @Query("SELECT * FROM factors ORDER BY id")
    fun getFactors(): LiveData<List<Factor>>

    @Query("SELECT * FROM music WHERE type = '$TYPE_MUSIC' ORDER BY duration")
    fun getAllMusic(): LiveData<List<Music>>

    @Query("SELECT * FROM music WHERE type = '$TYPE_MUSIC' AND duration > 0 ORDER BY duration")
    fun getRelaxMusic(): LiveData<List<Music>>

    @Query("SELECT * FROM music WHERE type = '$TYPE_ALARM' ORDER BY duration")
    fun getAlarms(): LiveData<List<Music>>

    @Query("SELECT * FROM recordings WHERE id = :recId")
    fun getRecording(recId: Int): LiveData<Recording>

    @Query("SELECT * FROM recordings ORDER BY id DESC")
    fun getAllRecordings(): LiveData<List<Recording>>

    @Query("SELECT * FROM recordings WHERE sleep_at_time BETWEEN :endTime AND :startTime")
    fun getRecordingsBetween(startTime: Long, endTime: Long): LiveData<List<Recording>>
}