package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import kotlinx.android.synthetic.main.recording_item.view.*
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*


class RecordingsRecyclerAdapter(
    private var recordings: List<Recording>,
    private var context: Context
) : RecyclerView.Adapter<RecordingsRecyclerAdapter.RecordingsHolder>() {

    var onItemClick: ((Recording) -> Unit)? = null

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): RecordingsHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.recording_item, viewGroup, false)
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
        recordingsViewHolder.date.text = convertLongToTime(recording.sleep_at_time!!)
        if (recording.rating != null) {
            recordingsViewHolder.rating.rating = recording.rating.toFloat()
        }
    }

    private fun convertLongToTime(time: Long): String {
        val date = Date(time)
        val format = DateFormat.getDateInstance()
        return format.format(date)
    }

    inner class RecordingsHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val date: TextView by lazy { view.recording_date_text }
        val rating: RatingBar by lazy { view.recording_rating }

        init {
            itemView.setOnClickListener {
                    onItemClick?.invoke(recordings[adapterPosition])
            }
        }
    }
}