package com.mindyapps.slimbo.ui.sleeping

import android.animation.ObjectAnimator
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.view.MotionEvent
import android.view.MotionEvent.ACTION_DOWN
import android.view.MotionEvent.ACTION_UP
import android.view.View
import android.view.animation.DecelerateInterpolator
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.preference.PreferenceManager
import com.gauravbhola.ripplepulsebackground.RipplePulseLayout
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.mindyapps.slimbo.MainActivity
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.RecorderService
import com.mindyapps.slimbo.RecorderService.START_ACTION
import com.mindyapps.slimbo.RecorderService.STOP_ACTION
import com.mindyapps.slimbo.data.model.AudioRecord
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.preferences.SleepingStore
import com.mindyapps.slimbo.ui.recording.RecordingFragment
import java.io.File


class SleepingActivity : AppCompatActivity(), View.OnClickListener, View.OnTouchListener {

    private lateinit var musicButton: FloatingActionButton
    private lateinit var musicText: TextView
    private lateinit var tipText: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var sleepingStore: SleepingStore
    private lateinit var broadcastReceiver: BroadcastReceiver
    private lateinit var ripplePulseLayout: RipplePulseLayout
    private lateinit var handler: Handler
    private var music: Music? = null
    private var factors: ArrayList<Factor>? = null
    private var lastPlayerPosition = 0
    private var duration: String? = null
    private var player: MediaPlayer? = null
    private var openDetails: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeping)
        sleepingStore =
            SleepingStore(PreferenceManager.getDefaultSharedPreferences(applicationContext))

        musicText = findViewById(R.id.music_text)
        musicButton = findViewById(R.id.music_button)
        tipText = findViewById(R.id.sleeping_tip_text)
        progressBar = findViewById(R.id.long_click_progress)
        ripplePulseLayout = findViewById(R.id.layout_ripplepulse)
        ripplePulseLayout.startRippleAnimation()
        handler = Handler()

        musicButton.setOnClickListener(this)
        progressBar.setOnTouchListener(this)

        music = intent.getParcelableExtra("music")
        duration = intent.getStringExtra("duration")
        factors = intent.getParcelableArrayListExtra("factors")

        if (factors != null) {
            Log.d("qwwe", "factors: ${factors}")
        }

        if (music != null && music!!.name != getString(R.string.do_not_use) && !sleepingStore.isWorking) {
            musicText.text = music!!.name
            ripplePulseLayout.visibility = View.VISIBLE
            if (!music!!.free!!) {
                val storagePath = File(externalCacheDir!!.absolutePath, "Music")
                if (!storagePath.exists()) {
                    storagePath.mkdirs()
                }
                val audioFile = File(storagePath, "${music!!.fileName}.mp3")
                    player = MediaPlayer.create(this, Uri.parse(audioFile.path))
                    player!!.start()
            } else {
                val resID = resources.getIdentifier(music!!.fileName, "raw", packageName)
                player = MediaPlayer.create(this, resID)
                player!!.start()
            }
            player!!.isLooping = true
            player!!.start()
            handler = Handler()
            handler.postDelayed(stopPlayerTask, getLength(duration))
            tipText.text = getString(R.string.monitoring_will_start) + " $duration"
            hideTip()
        }

        if (!sleepingStore.isWorking && music == null) {
            Handler().postDelayed(stopPlayerTask, 0)
            Handler().postDelayed(stopPlayerTask, 1000)
            hideTip()
        }

        broadcastReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent) {
                val message = intent.getStringExtra(RECEIVER_MESSAGE)
                if (message == "stop") {
                    Log.d("qwwe", "GOT STOP MESSAGE")
                    sleepingStore.isWorking = false
                    factors = intent.getParcelableArrayListExtra(SELECTED_FACTORS)
                    Log.d("qwwe", "factors: $factors")
                    Log.d("qwwe", "started at : ${intent.getLongExtra(START_TIME, 0)}")
                    Log.d("qwwe", "ended at : ${intent.getLongExtra(END_TIME, 0)}")


                    val recording = Recording(null, null, factors, null,
                        intent.getLongExtra(END_TIME, 0) - intent.getLongExtra(START_TIME, 0),
                        intent.getLongExtra(START_TIME, 0),
                        intent.getLongExtra(END_TIME, 0))

                    //if (openDetails){
                        val intent = Intent(context!!, MainActivity::class.java)
                        intent.putExtra("recording", recording)
                        startActivityForResult(intent, 1)
                    //}
                    LocalBroadcastManager.getInstance(context).unregisterReceiver(broadcastReceiver)
                    finish()
                }
            }
        }
    }

    private var stopPlayerTask = Runnable {
        if (player != null) {
            player!!.stop()
        }
        startService()
    }

    private fun hideTip() {
        val handlerText = Handler()
        handlerText.postDelayed({
            tipText.animate().alpha(0.0f)
        }, 5000)
    }

    private fun getLength(length: String?): Long {
        return when (length) {
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
        serviceIntent.putExtra(SELECTED_FACTORS, factors)
        ContextCompat.startForegroundService(this, serviceIntent)
        sleepingStore.isWorking = true
    }

    private fun stopService(openDetailsFragment: Boolean) {
        openDetails = openDetailsFragment
        val stopIntent = Intent(this, RecorderService::class.java)
        stopIntent.action = STOP_ACTION
        startService(stopIntent)
        sleepingStore.isWorking = false
        if (player != null) {
            player!!.stop()
        }
    }

    private fun showDialog() {
        val builder = AlertDialog.Builder(this, R.style.AlertDialogCustom)
        with(builder)
        {
            setTitle(getString(R.string.title_alert))
            setMessage(getString(R.string.sleep_quit_text))
            setPositiveButton(android.R.string.yes) { _, _ ->
                stopService(false)
            }
            setNegativeButton(android.R.string.no) { dialog, _ ->
                dialog.dismiss()
            }
            show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        handler.removeCallbacksAndMessages(null)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.music_button -> {
                if (player!!.isPlaying) {
                    player!!.pause()
                    ripplePulseLayout.stopRippleAnimation()
                    lastPlayerPosition = player!!.currentPosition
                } else {
                    ripplePulseLayout.startRippleAnimation()
                    player!!.seekTo(lastPlayerPosition)
                    player!!.start()
                }
            }
        }
    }

    override fun onStart() {
        super.onStart()
        LocalBroadcastManager.getInstance(this).registerReceiver(
            (broadcastReceiver),
            IntentFilter(RECEIVER_INTENT)
        )
    }

    override fun onBackPressed() {}

    companion object {
        const val RECEIVER_INTENT = "RECEIVER_INTENT"
        const val RECEIVER_MESSAGE = "RECEIVER_MESSAGE"
        const val SELECTED_FACTORS = "SELECTED_FACTORS"
        const val START_TIME = "START_TIME"
        const val END_TIME = "END_TIME"
    }

    override fun onTouch(p0: View?, event: MotionEvent?): Boolean {
        var animation = ObjectAnimator.ofInt(progressBar, "progress", 70)
        if (event!!.action == ACTION_DOWN) {
            animation.duration = 1000
            animation.interpolator = DecelerateInterpolator()
            animation.start()
        } else if (event.action == ACTION_UP) {
            if (progressBar.progress == 70) {
                if (!sleepingStore.minimalTimeReached) {
                    showDialog()
                } else {
                    animation.pause()
                    stopService(true)
                }
            }
            animation = ObjectAnimator.ofInt(progressBar, "progress", 0)
            animation.duration = 1000
            animation.interpolator = DecelerateInterpolator()
            animation.start()

        }
        return true
    }

}
