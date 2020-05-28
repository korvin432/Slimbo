package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.internal.DottedSeekBar
import kotlinx.android.synthetic.main.recording_item.view.*
import java.util.*


class RecordingsRecyclerAdapter(
    private var recordings: List<Recording>,
    private var context: Context
) : RecyclerView.Adapter<RecordingsRecyclerAdapter.RecordingsHolder>() {

    var onItemClick: ((Recording) -> Unit)? = null
    lateinit var colorsArray: List<Int>

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecordingsHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.recording_item, viewGroup, false)

        colorsArray = listOf(
            R.color.colorFirstCard,
            R.color.colorSecondCard,
            R.color.colorThirdCard,
            R.color.colorFourthCard,
            R.color.colorFifthCard)

        return RecordingsHolder(itemView)
    }

    override fun getItemCount(): Int {
        return recordings.size
    }

    override fun onBindViewHolder(holder: RecordingsHolder, position: Int) {
        val recording: Recording = recordings[position]
        setPropertiesForRecordingsViewHolder(holder, recording)
    }

    private fun setPropertiesForRecordingsViewHolder(
        recordingsViewHolder: RecordingsHolder,
        recording: Recording
    ) {
        var timeFormat = "HH:mm"
        if (!DateFormat.is24HourFormat(context)) timeFormat = "hh:mm a"

        recordingsViewHolder.sleepAt.text = convertDate(recording.sleep_at_time!!, timeFormat)
        recordingsViewHolder.date.text =
            DateFormat.format("MMM dd", Date(recording.sleep_at_time)).toString()
        if (recording.rating != null) {
            recordingsViewHolder.rating.rating = recording.rating.toFloat()
        }

        recordingsViewHolder.progress.thumb.mutate().alpha = 0
        recordingsViewHolder.progress.setPadding(0, 0, 0, 0)
        recordingsViewHolder.progress.max =
            ((recording.wake_up_time!! / 1000) - (recording.sleep_at_time / 1000)).toInt() / 2
        val dotList: MutableList<Int> = mutableListOf()
        recording.recordings!!.forEach {
            dotList.add(((it.creation_date!! / 1000) - (recording.sleep_at_time / 1000)).toInt() / 2)
        }
        recordingsViewHolder.progress.setDots(dotList.toIntArray())
        recordingsViewHolder.progress.setDotsDrawable(R.drawable.vertical_line)

        recordingsViewHolder.layout.backgroundTintList =
            ContextCompat.getColorStateList(context, colorsArray.random())
    }

    private fun convertDate(dateInMilliseconds: Long, dateFormat: String): String {
        return DateFormat.format(dateFormat, dateInMilliseconds).toString()
    }

    inner class RecordingsHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val sleepAt: TextView by lazy { view.recording_date_text }
        val date: Chip by lazy { view.date_chip }
        val rating: RatingBar by lazy { view.recording_rating }
        val progress: DottedSeekBar by lazy { view.sleep_progress }
        val layout: RelativeLayout by lazy { view.rec_layout }

        init {
            itemView.setOnClickListener {
                onItemClick?.invoke(recordings[adapterPosition])
            }
        }
    }
}