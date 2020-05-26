package com.mindyapps.slimbo.ui.recording

import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RatingBar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.AudioRecord
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.DottedSeekBar
import com.mindyapps.slimbo.ui.adapters.FactorsRecyclerAdapter
import kotlinx.android.synthetic.main.recording_fragment.*
import kotlinx.coroutines.launch


class RecordingFragment : Fragment() {
    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: RecordingViewModel
    private lateinit var sleepRatingBar: RatingBar
    private lateinit var recording: Recording
    private lateinit var factorsRecycler: RecyclerView
    private lateinit var factorsRecyclerAdapter: FactorsRecyclerAdapter
    private var audioRecords: MutableList<AudioRecord>? = null
    private lateinit var progress: DottedSeekBar

    private val sourceList = ArrayList<Factor>()
    private var selectedFactors = ArrayList<Factor>()
    private lateinit var observerFactors: Observer<List<Factor>>

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
        sleepRatingBar = root.findViewById(R.id.sleep_rating)

        if (recording.rating == null) {
            sleepRatingBar.setIsIndicator(false)
            sleepRatingBar.setOnRatingBarChangeListener { ratingBar, fl, b ->
                viewModel.updateRecording(recording.id!!, fl.toInt())
            }
        }


        bindRecyclerView()
        setUpSnoreProgress()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sleepAt = convertDate(recording.sleep_at_time!!, "HH:mm")
        val wakeUpAt = convertDate(recording.wake_up_time!!, "HH:mm")

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
    }

    fun convertDate(dateInMilliseconds: Long, dateFormat: String): String {
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
            this, RecordingViewModelFactory(repository, requireActivity().application)
        ).get(RecordingViewModel::class.java)

                factorsRecyclerAdapter.setFactors(selectedFactors)
    }

    private fun bindRecyclerView() {
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


}
