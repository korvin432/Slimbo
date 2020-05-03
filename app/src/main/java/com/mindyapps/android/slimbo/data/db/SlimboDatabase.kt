package com.mindyapps.android.slimbo.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.mindyapps.android.slimbo.data.model.Factor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


@Database(
    entities = [Factor::class],
    version = 2
)
abstract class SlimboDatabase : RoomDatabase() {
    abstract fun slimboDao(): SlimboDao

    companion object {
        @Volatile
        private var INSTANCE: SlimboDatabase? = null
        var firstrun = false

        fun getDatabase(context: Context): SlimboDatabase {
            val tempInstance = INSTANCE
            if (tempInstance != null) {
                return tempInstance
            }
            val rdc: Callback = object : Callback() {
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    firstrun = true
                }
            }
            synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    SlimboDatabase::class.java,
                    "slimbo_database"
                )
                    .addCallback(rdc)
                    .fallbackToDestructiveMigration()
                    .build()
                instance.openHelper.writableDatabase
                if (firstrun) {
                    importData(instance)
                }
                INSTANCE = instance

                return instance
            }
        }

        private fun importData(db: SlimboDatabase) {
            val factors = mutableListOf<Factor>()
            factors.add(Factor(null, "coffee", "ic_coffee"))
            factors.add(Factor(null, "disease", "ic_ill"))
            factors.add(Factor(null, "nose", "ic_nose"))
            factors.add(Factor(null, "shower", "ic_showe"))
            factors.add(Factor(null, "wine", "ic_wine"))
            factors.add(Factor(null, "workout", "ic_workout"))
            CoroutineScope(IO).launch {
                db.slimboDao().insertAllFactors(factors)
            }
        }
    }


}