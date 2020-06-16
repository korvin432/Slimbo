package com.mindyapps.slimbo.ui

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.mindyapps.slimbo.MainActivity
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.RecorderService
import com.mindyapps.slimbo.data.db.SlimboDatabase
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.preferences.AlarmStore
import com.mindyapps.slimbo.ui.sleeping.SleepingActivity.Companion.FROM_ALARM
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AlarmActivity : AppCompatActivity() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var stopButton: MaterialButton
    private lateinit var alarmStore: AlarmStore
    private lateinit var player: MediaPlayer
    private var repeatDelay: Int = 0
    private lateinit var handler: Handler
    private lateinit var rec: Recording

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("qwwe", "oncreate")
        setContentView(R.layout.activity_alarm)

        if (intent.getParcelableExtra<Recording>("recording") != null){
            rec = intent.getParcelableExtra("recording")!!
        }

        alarmStore = AlarmStore(
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        when (alarmStore.repeatMinutes) {
            1 -> repeatDelay = 5 * 60000
            2 -> repeatDelay = 10 * 60000
            3 -> repeatDelay = 15 * 60000
            4 -> repeatDelay = 20 * 60000
            5 -> repeatDelay = 30 * 60000
        }

        val selectedAlarm = Gson().fromJson(alarmStore.alarmSound, Music::class.java)
        val resID = resources.getIdentifier(selectedAlarm.fileName, "raw", packageName)
        player = MediaPlayer.create(this, resID)
        player.isLooping = true
        player.start()

        handler = Handler()
        handler.postDelayed(stopPlayerTask, 60000)

        stopButton = findViewById(R.id.stop_alarm_button)
        stopButton.setOnClickListener {
            player.stop()
            finish()
            openRecording(rec)
        }
    }

    private fun openRecording(recording: Recording) {
        CoroutineScope(Dispatchers.IO).launch {
            val slimboDao = SlimboDatabase.getDatabase(application).slimboDao()
            val id = repository.insertRecording(slimboDao, recording).toInt()

            withContext(Dispatchers.Main) {
                val newRec = Recording(
                    id,
                    recording.recordings,
                    recording.factors,
                    null,
                    recording.duration,
                    recording.wake_up_time,
                    recording.sleep_at_time
                )
                val intentRec = Intent(this@AlarmActivity, MainActivity::class.java)
                intentRec.putExtra("recording", newRec)
                startActivity(intentRec)
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacks(stopPlayerTask)
    }

    private var stopPlayerTask = Runnable {
        player.stop()
        if (repeatDelay != 0) {
            Handler().postDelayed({
                val selectedAlarm = Gson().fromJson(alarmStore.alarmSound, Music::class.java)
                val resID =
                    resources.getIdentifier(selectedAlarm.fileName, "raw", packageName)
                player = MediaPlayer.create(this, resID)
                player.isLooping = true
                player.start()
                val handler2 = Handler()
                handler2.postDelayed(stopFinalPlayerTask, 60000)
            }, repeatDelay.toLong())
        }
    }

    var stopFinalPlayerTask = Runnable { player.stop() }

}
