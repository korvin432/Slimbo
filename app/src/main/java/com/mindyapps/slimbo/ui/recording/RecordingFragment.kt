package com.mindyapps.slimbo.ui.recording

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.SeekBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.AudioRecord
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.DottedSeekBar


class RecordingFragment : Fragment() {
    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: RecordingViewModel
    private lateinit var sleepRatingBar: RatingBar
    private lateinit var recording: Recording
    private var audioRecords: MutableList<AudioRecord>? = null
    private lateinit var progress: DottedSeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recording = requireArguments().getParcelable("recording")!!
        audioRecords = recording.recordings
        (requireActivity() as AppCompatActivity).supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.activity_bg)))
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.recording_fragment, container, false)

        progress = root.findViewById(R.id.sleep_progress)
        sleepRatingBar = root.findViewById(R.id.sleep_rating)

        if (recording.rating == null) {
            sleepRatingBar.setIsIndicator(false)
            sleepRatingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
                viewModel.updateRecording(recording.id!!, fl.toInt())
            }
        }
        setUpSnoreProgress()



        return root
    }

    private fun setUpSnoreProgress() {
        progress.thumb.mutate().alpha = 0
        progress.setPadding(0, 0, 0, 0)
        progress.max =
            ((recording.wake_up_time!! / 1000) - (recording.sleep_at_time!! / 1000)).toInt() / 2
        initMarkers()
    }

    private fun initMarkers() {
        val dotList: MutableList<Int> = mutableListOf()
        audioRecords!!.forEach {
            dotList.add(((it.creation_date!! / 1000) - (recording.sleep_at_time!! / 1000)).toInt() / 2)
        }
        progress.setDots(dotList.toIntArray())
        progress.setDotsDrawable(R.drawable.vertical_line)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(
            this, RecordingViewModelFactory(requireActivity().application)
        ).get(RecordingViewModel::class.java)
    }



}
