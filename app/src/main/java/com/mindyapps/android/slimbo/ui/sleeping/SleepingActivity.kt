package com.mindyapps.android.slimbo.ui.sleeping

import android.os.Bundle
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.internal.Recorder
import kotlinx.android.synthetic.main.activity_sleeping.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.launch


class SleepingActivity : AppCompatActivity(), View.OnClickListener {

    var recorder: Recorder = Recorder.getInstance()

    lateinit var startButton: Button
    lateinit var stopButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sleeping)

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
