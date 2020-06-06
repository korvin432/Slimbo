package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
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

    var mediaPlayer: MediaPlayer? = null
    private var timer: Timer? = null
    private var snoreHolder: SnoreHolder? = null
    private var timeFormat = "HH:mm"

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): SnoreHolder {
        val itemView: View = LayoutInflater.from(viewGroup.context)
            .inflate(R.layout.music_item, viewGroup, false)
        if (!DateFormat.is24HourFormat(context)){
            timeFormat = "hh:mm a"
        }
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
        try {
            snoreViewHolder.waveView.setRawData(File(snore.file_name!!).readBytes())
        } catch (ex: Exception){
            snoreViewHolder.notFoundText.visibility = View.VISIBLE
            snoreViewHolder.waveView.visibility = View.GONE
            snoreViewHolder.playButton.visibility = View.GONE
        }
        snoreViewHolder.createdAtText.text = convertDate(snore.creation_date!!, timeFormat)

        snoreViewHolder.durationText.text =
            String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(snore.duration!!),
                TimeUnit.MILLISECONDS.toSeconds(snore.duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(snore.duration)))
        snoreViewHolder.snore = snore
    }

    var timerTask = object : TimerTask() {
        override fun run() {
            val audioWaveProgress: Float =
                mediaPlayer!!.currentPosition / mediaPlayer!!.duration.toFloat() * 100f
            snoreHolder!!.waveView.progress = audioWaveProgress
        }
    }

    private fun convertDate(dateInMilliseconds: Long, dateFormat: String): String {
        return DateFormat.format(dateFormat, dateInMilliseconds).toString()
    }

    inner class SnoreHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val playButton: ImageView by lazy { view.play_snore_btn }
        val createdAtText: TextView by lazy { view.created_at_text }
        val durationText: TextView by lazy { view.duration_text }
        val notFoundText: TextView by lazy { view.not_found_text }
        val waveView: AudioWaveView by lazy { view.waveView }

        lateinit var snore: AudioRecord

        init {
            playButton.setOnClickListener {
                snoreHolder = this
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.seekTo(0)
                    mediaPlayer!!.pause()
                    waveView.progress = 0f
                    playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_play))
                } else {
                    playButton.setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_stop))
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
                    if (byUser && mediaPlayer != null) {
                        mediaPlayer!!.seekTo((mediaPlayer!!.duration * (progress / 100)).toInt())
                    }
                }
                override fun onStartTracking(progress: Float) {}
                override fun onStopTracking(progress: Float) {}
            }

        }
    }


}