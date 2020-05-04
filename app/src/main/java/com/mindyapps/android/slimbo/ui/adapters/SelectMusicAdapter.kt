package com.mindyapps.android.slimbo.ui.adapters

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Music
import kotlinx.android.synthetic.main.select_music_item.view.*


class SelectedMusicAdapter(
    private var music: MutableList<Music>,
    private var selectedMusic: Music?,
    private var context: Context
) : RecyclerView.Adapter<SelectedMusicAdapter.MusicHolder>() {

    var onItemClick: ((Music) -> Unit)? = null

    private var lastChecked: CompoundButton? = null

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
        musicViewHolder.durationText.text = music.duration
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
        }
        if (music.name == context.getString(R.string.do_not_use)) {
            musicViewHolder.musicRadioButton.isChecked = true
        }
    }

    inner class MusicHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val musicRadioButton: RadioButton by lazy { view.music_select }
        val durationText: TextView by lazy { view.music_duration }

        init {
            musicRadioButton.setOnClickListener {
                onItemClick?.invoke(music[adapterPosition])
            }
        }
    }
}