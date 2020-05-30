package com.mindyapps.slimbo.ui.recording

import android.R.attr.duration
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.AudioRecord
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.DottedSeekBar
import com.mindyapps.slimbo.ui.adapters.FactorsRecyclerAdapter
import com.mindyapps.slimbo.ui.adapters.SnoreAdapter
import kotlinx.android.synthetic.main.recording_fragment.*
import kotlinx.coroutines.launch
import rm.com.audiowave.AudioWaveView
import rm.com.audiowave.OnProgressListener
import java.io.File
import java.util.*
import kotlin.collections.ArrayList


class RecordingFragment : Fragment() {
    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: RecordingViewModel
    private lateinit var sleepRatingBar: RatingBar
    private lateinit var recording: Recording
    private lateinit var factorsRecycler: RecyclerView
    private lateinit var snoreRecycler: RecyclerView
    private lateinit var factorsRecyclerAdapter: FactorsRecyclerAdapter
    private lateinit var snoreAdapter: SnoreAdapter
    private var audioRecords: MutableList<AudioRecord>? = null
    private lateinit var progress: DottedSeekBar
    private var selectedFactors = ArrayList<Factor>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recording = requireArguments().getParcelable("recording")!!
        audioRecords = recording.recordings
        selectedFactors = recording.factors as ArrayList<Factor>
        (requireActivity() as AppCompatActivity).supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.activity_bg))
        )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.recording_fragment, container, false)

        progress = root.findViewById(R.id.sleep_progress)
        factorsRecycler = root.findViewById(R.id.selected_factors_recycler)
        snoreRecycler = root.findViewById(R.id.snore_recycler)
        sleepRatingBar = root.findViewById(R.id.sleep_rating)

        if (recording.rating == null) {
            sleepRatingBar.setIsIndicator(false)
            sleepRatingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
                viewModel.updateRecording(recording.id!!, fl.toInt())
            }
        } else {
            sleepRatingBar.rating = recording.rating!!.toFloat()
            sleepRatingBar.setIsIndicator(true)
            root.findViewById<TextView>(R.id.how_do_you_feel_textView).visibility = View.GONE
            sleepRatingBar.isClickable = false
        }


        bindFactorsRecyclerView()
        setUpSnoreProgress()
        bindSnoreRecyclerView()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val timeFormat = if (!DateFormat.is24HourFormat(requireContext())) {
            "hh:mm a"
        } else {
            "HH:mm"
        }
        val sleepAt = convertDate(recording.sleep_at_time!!, timeFormat)
        val wakeUpAt = convertDate(recording.wake_up_time!!, timeFormat)

        var timeInSeconds: Int = (recording.duration!! / 1000).toInt()
        val hours: Int
        val minutes: Int
        hours = timeInSeconds / 3600
        timeInSeconds -= hours * 3600
        minutes = timeInSeconds / 60

        sleep_at.text = sleepAt
        avg_start.text = sleepAt
        wake_up.text = wakeUpAt
        avg_end.text = wakeUpAt
        sleep_time.text = String.format("%02d", hours) + ":" + String.format("%02d", minutes)

        checkEmptyData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (snoreAdapter.mediaPlayer != null && snoreAdapter.mediaPlayer!!.isPlaying) {
            snoreAdapter.mediaPlayer!!.stop()
        }
    }

    private fun checkEmptyData() {
        if (recording.recordings != null && recording.recordings!!.size <= 0) {
            no_snore_layout.visibility = View.VISIBLE
        }
        if (recording.factors != null && recording.factors!!.size <= 0) {
            no_factors_layout.visibility = View.VISIBLE
        }
    }

    private fun convertDate(dateInMilliseconds: Long, dateFormat: String): String {
        return DateFormat.format(dateFormat, dateInMilliseconds)
            .toString()
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
        factorsRecyclerAdapter.setFactors(selectedFactors)
    }

    private fun bindFactorsRecyclerView() {
        factorsRecyclerAdapter = FactorsRecyclerAdapter(
            selectedFactors.toMutableList(),
            selectedFactors.toMutableList(),
            requireActivity().applicationContext
        )
        factorsRecyclerAdapter.isClickable = false
        factorsRecycler.layoutManager =
            GridLayoutManager(requireActivity().applicationContext, 3, RecyclerView.VERTICAL, false)
        factorsRecycler.adapter = factorsRecyclerAdapter

    }

    private fun bindSnoreRecyclerView() {
        snoreAdapter = SnoreAdapter(recording.recordings!!, requireContext())
        snoreRecycler.layoutManager = LinearLayoutManager(requireContext())
        snoreRecycler.adapter = snoreAdapter
    }

}
