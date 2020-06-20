package com.mindyapps.asleep.data.db

import androidx.lifecycle.LiveData
import androidx.room.*
import com.mindyapps.asleep.data.model.*

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

    @Query("SELECT * FROM music WHERE type = '$TYPE_ALARM' OR type = '$TYPE_MUSIC' AND fileName != '' ORDER BY duration")
    fun getAlarms(): LiveData<List<Music>>

    @Query("SELECT * FROM recordings WHERE id = :recId")
    fun getRecording(recId: Int): LiveData<Recording>

    @Query("SELECT * FROM recordings ORDER BY id DESC")
    fun getAllRecordings(): LiveData<List<Recording>>

    @Query("SELECT COUNT(*) FROM recordings")
    fun getRecordingsCount(): LiveData<Int>

    @Query("SELECT * FROM recordings WHERE sleep_at_time BETWEEN :endTime AND :startTime")
    fun getRecordingsBetween(startTime: Long, endTime: Long): LiveData<List<Recording>>

    @Delete
    fun deleteRecording(recording: Recording)
}