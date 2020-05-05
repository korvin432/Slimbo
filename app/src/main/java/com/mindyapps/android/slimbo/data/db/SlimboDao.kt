package com.mindyapps.android.slimbo.data.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.mindyapps.android.slimbo.data.model.Factor
import com.mindyapps.android.slimbo.data.model.Music

@Dao
interface SlimboDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllFactors(factors: MutableList<Factor>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAllMusic(music: MutableList<Music>)

    @Query("SELECT * FROM factors ORDER BY id")
    fun getFactors(): LiveData<List<Factor>>

    @Query("SELECT * FROM music ORDER BY duration")
    fun getAllMusic(): LiveData<List<Music>>
}