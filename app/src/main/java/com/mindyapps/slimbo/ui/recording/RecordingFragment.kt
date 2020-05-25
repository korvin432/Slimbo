package com.mindyapps.slimbo.ui.recording

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.DottedSeekBar


class RecordingFragment : Fragment(){

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: RecordingViewModel
    private lateinit var sadImage: ImageView
    private lateinit var recording: Recording
    private lateinit var progress: DottedSeekBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        recording = requireArguments().getParcelable("recording")!!
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.recording_fragment, container, false)

        sadImage = root.findViewById(R.id.image_sad)

        progress = root.findViewById(R.id.sleep_progress)
        progress.thumb.mutate().alpha = 0
        progress.max = 1590414969000

        initMarkers()

        return root
    }

    private fun initMarkers() {
        progress.setDots(intArrayOf(1590372669000, 1590389109000, 1590404169000))
        progress.setDotsDrawable(R.drawable.vertical_line)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(
            this, RecordingViewModelFactory(
                repository, requireActivity().application
            )
        ).get(RecordingViewModel::class.java)
        // TODO: Use the ViewModel
    }


}
