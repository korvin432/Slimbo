package com.mindyapps.android.slimbo.ui.sleeping

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.RecorderService
import com.mindyapps.android.slimbo.RecorderService.START_ACTION
import com.mindyapps.android.slimbo.RecorderService.STOP_ACTION
import com.mindyapps.android.slimbo.internal.Recorder
import com.mindyapps.android.slimbo.preferences.AlarmStore
import com.mindyapps.android.slimbo.preferences.SleepingStore


class SleepingActivity : AppCompatActivity(), View.OnClickListener {

    private lateinit var musicButton: Button
    private lateinit var stopButton: Button

    private lateinit var sleepingStore: SleepingStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeping)


        sleepingStore = SleepingStore(
            PreferenceManager.getDefaultSharedPreferences(applicationContext)
        )

        musicButton = findViewById(R.id.music_button)
        stopButton = findViewById(R.id.stop_listen)
        musicButton.setOnClickListener(this)
        stopButton.setOnClickListener(this)

        if (!sleepingStore.isWorking) {
            startService()
        }
    }

    private fun startService() {
        val serviceIntent = Intent(this, RecorderService::class.java)
        serviceIntent.action = START_ACTION
        ContextCompat.startForegroundService(this, serviceIntent)
        sleepingStore.isWorking = true
    }

    private fun stopService() {
        val stopIntent = Intent(this, RecorderService::class.java)
        stopIntent.action = STOP_ACTION
        startService(stopIntent)
        sleepingStore.isWorking = false
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.music_button -> {
                //stop/start music
            }
            R.id.stop_listen -> {
                stopService()
            }
        }
    }

    override fun onBackPressed() {

    }

}
