package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.TYPE_ALARM
import kotlinx.android.synthetic.main.select_music_item.view.*
import java.io.File
import java.util.concurrent.TimeUnit


class SelectMusicAdapter(
    private var music: MutableList<Music>,
    private var selectedMusic: Music?,
    private var context: Context
) : RecyclerView.Adapter<SelectMusicAdapter.MusicHolder>() {

    var onItemClick: ((Music) -> Unit)? = null
    var lastChecked: CompoundButton? = null

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): MusicHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.select_music_item, viewGroup, false)
        return MusicHolder(itemView)
    }

    override fun getItemCount(): Int {
        return music.size
    }

    override fun onBindViewHolder(holder: MusicHolder, position: Int) {
        val music: Music = music[position]
        setPropertiesForMusicViewHolder(holder, music)
    }

    private fun setPropertiesForMusicViewHolder(
        musicViewHolder: MusicHolder,
        music: Music
    ) {
        musicViewHolder.musicRadioButton.text = music.name
        if (music.duration!! > 1 && music.type != TYPE_ALARM) {
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
                musicViewHolder.musicRadioButton.visibility = View.GONE
            }
        }
        
        musicViewHolder.musicRadioButton.setOnCheckedChangeListener { compoundButton, b ->
            if (lastChecked != null) {
                lastChecked!!.isChecked = false
            }
            lastChecked = compoundButton
        }
        if (selectedMusic?.name != null) {
            if (music.name == selectedMusic?.name) {
                musicViewHolder.musicRadioButton.isChecked = true
            }
        } else {
            if (music.name == context.getString(R.string.do_not_use)) {
                musicViewHolder.musicRadioButton.isChecked = true
            }
        }


    }

    inner class MusicHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val musicRadioButton: RadioButton by lazy { view.music_select }
        val durationText: TextView by lazy { view.music_duration }
        val paidLayout: LinearLayout by lazy { view.paid_layout }
        val musicText: TextView by lazy { view.music_name }

        init {
            musicRadioButton.setOnClickListener {
                onItemClick?.invoke(music[adapterPosition])
            }
            paidLayout.setOnClickListener {
                onItemClick?.invoke(music[adapterPosition])
            }
        }
    }
}