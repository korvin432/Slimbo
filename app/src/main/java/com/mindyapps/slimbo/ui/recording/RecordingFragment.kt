package com.mindyapps.slimbo.ui.recording

import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.AudioRecord
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.DottedSeekBar


class RecordingFragment : Fragment() {
//todo find some library for seekbar that can set min value and modify it to dotted (or replace with progressbar)
    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: RecordingViewModel
    private lateinit var sadImage: ImageView
    private lateinit var recording: Recording
    private var audioRecords: MutableList<AudioRecord>? = null
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
        progress.max = (recording.wake_up_time!! / 1000).toString().substring(4).toInt()
        Log.d("qwwe", "max: ${(recording.wake_up_time!! / 1000).toString().substring(4).toInt()}")

        audioRecords = recording.recordings

        initMarkers()

        return root
    }

    private fun initMarkers() {
        val dotList: MutableList<Int> = mutableListOf()
        audioRecords!!.forEach {
            dotList.add((it.creation_date!! / 1000).toString().substring(4).toInt())
            Log.d("qwwe", "adding: ${(it.creation_date / 1000).toString().substring(4).toInt()}")
        }
        Log.d("qwwe", "dots: ${dotList}")
        progress.setDots(dotList.toIntArray())
        progress.setDotsDrawable(R.drawable.vertical_line)
    }

    fun convertDate(dateInMilliseconds: Long,dateFormat: String?): Int {
        return DateFormat.format(dateFormat, dateInMilliseconds).toString().toInt()
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
