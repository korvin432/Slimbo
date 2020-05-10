package com.mindyapps.android.slimbo.ui.sleeping

import android.os.Bundle
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.internal.Recorder
import kotlinx.android.synthetic.main.activity_sleeping.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class SleepingActivity : AppCompatActivity(), View.OnClickListener {

    var recorder = Recorder()

    lateinit var startButton: Button
    lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeping)

        fullscreen_content.systemUiVisibility =
            View.SYSTEM_UI_FLAG_LOW_PROFILE or
                    View.SYSTEM_UI_FLAG_FULLSCREEN or
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
                    View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION

        startButton = findViewById(R.id.start_listen)
        stopButton = findViewById(R.id.stop_listen)
        startButton.setOnClickListener(this)
        stopButton.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.start_listen -> {
                CoroutineScope(IO).launch {
                    recorder.setActive(true)
                    recorder.arm()
                }
            }
            R.id.stop_listen -> {
                recorder.setActive(false)
            }
        }
    }


}
