package com.mindyapps.android.slimbo.ui.sleeping

import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.preference.PreferenceManager
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.RecorderService
import com.mindyapps.android.slimbo.RecorderService.START_ACTION
import com.mindyapps.android.slimbo.RecorderService.STOP_ACTION
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.preferences.SleepingStore


class SleepingActivity : AppCompatActivity(), View.OnClickListener {


    private lateinit var musicButton: Button
    private lateinit var stopButton: Button
    private lateinit var sleepingStore: SleepingStore
    private var music: Music? = null
    private var lastPlayerPosition = 0
    private var duration: String? = null
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeping)
        sleepingStore =
            SleepingStore(PreferenceManager.getDefaultSharedPreferences(applicationContext))

        musicButton = findViewById(R.id.music_button)
        stopButton = findViewById(R.id.stop_listen)
        musicButton.setOnClickListener(this)
        stopButton.setOnClickListener(this)

        music = intent.getParcelableExtra("music")
        duration = intent.getStringExtra("duration")

        if (music != null && music!!.name != getString(R.string.do_not_use)) {
            musicButton.visibility = View.VISIBLE
            val resID = resources.getIdentifier(music!!.fileName, "raw", packageName)
            player = MediaPlayer.create(this, resID)
            player!!.isLooping = true
            player!!.start()
            Handler().postDelayed(stopPlayerTask, getLength(duration))
        }

        if (!sleepingStore.isWorking && music == null) {
            startService()
        }
    }

    private var stopPlayerTask = Runnable {
        player!!.stop()
        startService()
    }

    private fun getLength(length: String?): Long{
        return when(length){
            getString(R.string.five_minutes) -> 300000
            getString(R.string.ten_minutes) -> 600000
            getString(R.string.twenty_minutes) -> 20 * 60000
            getString(R.string.thirty_minutes) -> 30 * 60000
            resources.getString(R.string.ffive_minutes) -> 45 * 60000
            else -> 0
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
        finish()
        //todo open details fragment
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.music_button -> {
                if (player!!.isPlaying){
                    player!!.pause()
                    lastPlayerPosition = player!!.currentPosition
                } else {
                    player!!.seekTo(lastPlayerPosition)
                    player!!.start()
                }
            }
            R.id.stop_listen -> {
                stopService()
            }
        }
    }

    override fun onStop() {
        super.onStop()
        if (player != null){
            player!!.stop()
        }
    }

    override fun onBackPressed() { }

}
