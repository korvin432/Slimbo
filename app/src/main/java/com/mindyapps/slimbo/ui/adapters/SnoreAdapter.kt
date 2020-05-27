package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.AudioRecord
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.TYPE_ALARM
import kotlinx.android.synthetic.main.activity_sleeping.view.*
import kotlinx.android.synthetic.main.music_item.view.*
import kotlinx.android.synthetic.main.select_music_item.view.*
import rm.com.audiowave.AudioWaveView
import rm.com.audiowave.OnProgressListener
import java.io.File
import java.util.*
import java.util.concurrent.TimeUnit


class SnoreAdapter(
    private var snoreList: MutableList<AudioRecord>,
    private var context: Context
) : RecyclerView.Adapter<SnoreAdapter.SnoreHolder>() {

    private var mediaPlayer: MediaPlayer? = null
    var onItemClick: ((AudioRecord) -> Unit)? = null
    private var timer: Timer? = null
    private var snoreHolder: SnoreHolder? = null

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): SnoreHolder {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.music_item, viewGroup, false)
        return SnoreHolder(itemView)
    }

    override fun getItemCount(): Int {
        return snoreList.size
    }

    override fun onBindViewHolder(holder: SnoreHolder, position: Int) {
        val snore: AudioRecord = snoreList[position]
        snoreHolder = holder
        setPropertiesForSnoreViewHolder(holder, snore)
    }

    private fun setPropertiesForSnoreViewHolder(
        snoreViewHolder: SnoreHolder,
        snore: AudioRecord
    ) {
        snoreViewHolder.waveView.setRawData(File(snore.file_name!!).readBytes())
        snoreViewHolder.snore = snore
    }

    var timerTask = object : TimerTask() {
        override fun run() {
            val audioWaveProgress: Float =
                mediaPlayer!!.currentPosition / mediaPlayer!!.duration.toFloat() * 100f
            snoreHolder!!.waveView.progress = audioWaveProgress
        }
    }

    inner class SnoreHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val playButton: Button by lazy { view.play_snore_btn }
        val waveView: AudioWaveView by lazy { view.waveView }

        lateinit var snore: AudioRecord

        init {
            playButton.setOnClickListener {
                snoreHolder = this
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.pause()
                } else {
                    mediaPlayer = MediaPlayer.create(context, Uri.parse(snore.file_name))
                    waveView.setRawData(File(snore.file_name!!).readBytes())
                    if (timer == null) {
                        timer = Timer()
                        timer!!.schedule(timerTask, 0, 1000)
                    }
                    mediaPlayer!!.start()
                }
            }
            waveView.onProgressListener = object : OnProgressListener {
                override fun onProgressChanged(progress: Float, byUser: Boolean) {
                    if (byUser) {
                        mediaPlayer!!.seekTo((mediaPlayer!!.duration * (progress / 100)).toInt())
                    }
                }
                override fun onStartTracking(progress: Float) {}
                override fun onStopTracking(progress: Float) {}
            }

        }
    }


}