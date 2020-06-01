package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.media.MediaPlayer
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.TYPE_MUSIC
import kotlinx.android.synthetic.main.relax_music_item.view.*
import kotlinx.android.synthetic.main.select_music_item.view.music_duration
import kotlinx.android.synthetic.main.select_music_item.view.music_name
import kotlinx.android.synthetic.main.select_music_item.view.paid_layout
import java.io.File
import java.util.concurrent.TimeUnit


class RelaxMusicAdapter(
    private var music: MutableList<Music>,
    private var context: Context
) : RecyclerView.Adapter<RelaxMusicAdapter.MusicHolder>() {

    var mediaPlayer: MediaPlayer? = null
    var onItemClick: ((Music) -> Unit)? = null
    private var musicHolder: MusicHolder? = null
    private var previousMusicHolder: MusicHolder? = null


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): MusicHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.relax_music_item, viewGroup, false)
        return MusicHolder(itemView)
    }

    override fun getItemCount(): Int {
        return music.size
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {
        val music: Music = music[position]
        musicHolder = holder
        setPropertiesForMusicViewHolder(holder, music)
    }

    private fun setPropertiesForMusicViewHolder(
        musicViewHolder: MusicHolder,
        music: Music
    ) {
        musicViewHolder.musicItem = music
        if (music.name != context.getString(R.string.do_not_use)) {
            musicViewHolder.musicName.text = music.name
            if (music.duration!! > 1 && music.type == TYPE_MUSIC) {
                musicViewHolder.durationText.text = String.format(
                    "%02d:%02d",
                    TimeUnit.MILLISECONDS.toMinutes(music.duration),
                    TimeUnit.MILLISECONDS.toSeconds(music.duration) -
                            TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(music.duration))
                )
            }

            if (!music.free!!) {
                val storagePath = File(context.externalCacheDir!!.absolutePath, "Music")
                if (!storagePath.exists()) {
                    storagePath.mkdirs()
                }
                val audioFile = File(storagePath, "${music.fileName}.mp3")
                if (!audioFile.exists()) {
                    musicViewHolder.paidLayout.visibility = View.VISIBLE
                    musicViewHolder.musicText.text = music.name
                    musicViewHolder.playButton.visibility = View.GONE
                    musicViewHolder.musicName.visibility = View.GONE
                }
            }
        }
    }

    inner class MusicHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val playButton: ImageView by lazy { view.play_music_btn }
        val musicName: TextView by lazy { view.relax_music_name }
        val durationText: TextView by lazy { view.music_duration }
        val paidLayout: LinearLayout by lazy { view.paid_layout }
        val musicText: TextView by lazy { view.music_name }

        lateinit var musicItem: Music

        init {
            playButton.setOnClickListener {
                previousMusicHolder = musicHolder
                if (previousMusicHolder != null){
                    previousMusicHolder!!.playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_play
                        )
                    )
                }
                musicHolder = this
                if (mediaPlayer != null && mediaPlayer!!.isPlaying) {
                    mediaPlayer!!.seekTo(0)
                    mediaPlayer!!.pause()
                    playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_play
                        )
                    )
                } else {
                    if (!musicItem.free!!) {
                        val storagePath =
                            File(context.externalCacheDir!!.absolutePath, "Music")
                        if (!storagePath.exists()) {
                            storagePath.mkdirs()
                        }
                        val audioFile = File(storagePath, "${musicItem.fileName}.mp3")
                        if (audioFile.exists()) {
                            mediaPlayer = MediaPlayer.create(context, Uri.parse(audioFile.path))
                        }
                    } else {
                        val resID =
                            context.resources.getIdentifier(
                                musicItem.fileName,
                                "raw",
                                context.packageName
                            )
                        mediaPlayer = MediaPlayer.create(context, resID)

                    }
                    playButton.setImageDrawable(
                        ContextCompat.getDrawable(
                            context,
                            R.drawable.ic_stop
                        )
                    )
                    mediaPlayer!!.start()
                }
            }
            paidLayout.setOnClickListener {
                onItemClick?.invoke(music[adapterPosition])
            }
        }
    }
}