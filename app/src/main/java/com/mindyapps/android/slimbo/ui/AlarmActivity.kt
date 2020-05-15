package com.mindyapps.android.slimbo.ui

import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.google.android.material.button.MaterialButton
import com.google.gson.Gson
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.preferences.AlarmStore
import com.mindyapps.android.slimbo.internal.Recorder


class AlarmActivity : AppCompatActivity() {

    private lateinit var stopButton: MaterialButton
    private lateinit var alarmStore: AlarmStore
    private lateinit var player: MediaPlayer
    private var repeatDelay: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("qwwe", "oncreate")
        Recorder.getInstance().setActive(false)
        setContentView(R.layout.activity_alarm)

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

        val handler = Handler()
        handler.postDelayed(stopPlayerTask, 60000)

        stopButton = findViewById(R.id.stop_alarm_button)
        stopButton.setOnClickListener {
            player.stop()
        }


    }

    var stopPlayerTask = Runnable {
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
