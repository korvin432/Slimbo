package com.mindyapps.android.slimbo.ui.dialogs.alarm

import android.app.TimePickerDialog
import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.ui.internal.Utils
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.android.slimbo.ui.adapters.SelectedMusicAdapter
import kotlinx.coroutines.launch
import java.util.*

class AlarmFragment : DialogFragment(), CompoundButton.OnCheckedChangeListener {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: AlarmViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: MaterialButton
    private lateinit var firstAlarmPicker: CheckBox
    private lateinit var secondAlarmPicker: CheckBox
    private lateinit var thirdAlarmPicker: CheckBox
    private lateinit var selectedMusicAdapter: SelectedMusicAdapter
    private lateinit var timeSetListener: TimePickerDialog.OnTimeSetListener

    private lateinit var observerMusic: Observer<List<Music>>
    private var selectedAlarm: Music? = null
    private var firstAlarm: IntArray? = null
    private var secondAlarm: IntArray? = null
    private var thirdAlarm: IntArray? = null
    private var moved = false
    var player: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.alarm_fragment, container, false)
        this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)

        viewModel = ViewModelProvider(
            this,
            AlarmViewModelFactory(repository, requireActivity().application)
        ).get(AlarmViewModel::class.java)


        recyclerView = root.findViewById(R.id.alarm_recycler)
        confirmButton = root.findViewById(R.id.confirm_alarm_button)
        firstAlarmPicker = root.findViewById(R.id.first_alarm_picker)
        secondAlarmPicker = root.findViewById(R.id.second_alarm_picker)
        thirdAlarmPicker = root.findViewById(R.id.third_alarm_picker)


        if (requireArguments().getParcelable<Music>("selected_alarm_sound") != null) {
            selectedAlarm = requireArguments().getParcelable("selected_alarm_sound")
            firstAlarm = requireArguments().getIntArray("first_alarm")
            firstAlarmPicker.isChecked = true
            firstAlarmPicker.text = "${firstAlarm!![0]}:${firstAlarm!![1]}"
            secondAlarmPicker.alpha = 1f
            secondAlarmPicker.isClickable = true

            secondAlarm = requireArguments().getIntArray("second_alarm")
            if (secondAlarm != null) {
                secondAlarmPicker.isChecked = true
                thirdAlarmPicker.alpha = 1f
                thirdAlarmPicker.isClickable = true
                secondAlarmPicker.text = "${secondAlarm!![0]}:${secondAlarm!![1]}"
            }
            thirdAlarm = requireArguments().getIntArray("third_alarm")
            if (thirdAlarm != null) {
                thirdAlarmPicker.alpha = 1f
                thirdAlarmPicker.isClickable = true
                thirdAlarmPicker.isChecked = true
                thirdAlarmPicker.text = "${thirdAlarm!![0]}:${thirdAlarm!![1]}"
            }
        }

        firstAlarmPicker.setOnCheckedChangeListener(this)
        secondAlarmPicker.setOnCheckedChangeListener(this)
        thirdAlarmPicker.setOnCheckedChangeListener(this)

        confirmButton.setOnClickListener {
            if (selectedAlarm != null) {
                lifecycleScope.launch {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "first_alarm", firstAlarm
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "second_alarm", secondAlarm
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "third_alarm", thirdAlarm
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_alarm_sound", selectedAlarm
                    )
                }
            }
            dismiss()
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observerMusic = Observer { newsSource ->
            if (newsSource.isNotEmpty()) {
                bindRecyclerView(newsSource)
            }
        }
        loadMusic()
    }

    private fun loadMusic() {
        lifecycleScope.launch {
            viewModel.allAlarms.observe(viewLifecycleOwner, observerMusic)
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        stopPlaying()
    }

    private fun stopPlaying() {
        if (player != null) {
            if (player!!.isPlaying)
                player!!.stop()
        }
    }

    private fun bindRecyclerView(musicList: List<Music>) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(musicList.size)
        selectedMusicAdapter = SelectedMusicAdapter(
            musicList.toMutableList(), selectedAlarm, requireActivity().applicationContext
        )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = selectedMusicAdapter
        selectedMusicAdapter.onItemClick = { music ->
            stopPlaying()
            val resID =
                resources.getIdentifier(music.fileName, "raw", requireContext().packageName)
            player = MediaPlayer.create(requireContext(), resID)
            player!!.start()
            selectedAlarm = music
        }
    }


    fun showPicker(cal: Calendar) {
        if (DateFormat.is24HourFormat(requireContext())) {
            TimePickerDialog(
                requireContext(), timeSetListener, cal.get(Calendar.HOUR_OF_DAY),
                cal.get(Calendar.MINUTE), true
            ).show()
        } else {
            TimePickerDialog(
                requireContext(), timeSetListener, cal.get(Calendar.HOUR), cal.get(Calendar.MINUTE),
                false
            ).show()
        }
    }

    override fun onCheckedChanged(checkBox: CompoundButton?, checked: Boolean) {
        val cal = Calendar.getInstance()

        when (checkBox!!.id) {
            R.id.first_alarm_picker -> {
                if (checkBox.isChecked) {
                    timeSetListener =
                        TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                            firstAlarmPicker.text =
                                Utils(
                                    requireContext()
                                ).getAlarmTime(intArrayOf(hour, minute))
                            firstAlarm = intArrayOf(hour, minute)
                            secondAlarmPicker.alpha = 1f
                            secondAlarmPicker.isClickable = true
                        }
                    showPicker(cal)
                } else {
                    secondAlarmPicker.alpha = 0.5f
                    secondAlarmPicker.isClickable = false
                    thirdAlarmPicker.alpha = 0.5f
                    thirdAlarmPicker.isClickable = false
                    firstAlarm = null
                    secondAlarm = null
                    thirdAlarm = null
                }
            }
            R.id.second_alarm_picker -> {
                if (checkBox.isChecked) {
                    timeSetListener =
                        TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                            secondAlarmPicker.text =
                                Utils(
                                    requireContext()
                                ).getAlarmTime(intArrayOf(hour, minute))
                            secondAlarm = intArrayOf(hour, minute)
                            thirdAlarmPicker.alpha = 1f
                            thirdAlarmPicker.isClickable = true
                        }
                    showPicker(cal)
                } else {
                    thirdAlarmPicker.alpha = 0.5f
                    thirdAlarmPicker.isClickable = false
                    thirdAlarmPicker.isChecked = false
                    secondAlarm = null
                    thirdAlarm = null
                }
            }
            R.id.third_alarm_picker -> {
                if (checkBox.isChecked) {
                    timeSetListener =
                        TimePickerDialog.OnTimeSetListener { timePicker, hour, minute ->
                            thirdAlarmPicker.text =
                                Utils(
                                    requireContext()
                                ).getAlarmTime(intArrayOf(hour, minute))
                            thirdAlarm = intArrayOf(hour, minute)
                            thirdAlarmPicker.alpha = 1f
                            thirdAlarmPicker.isClickable = true
                        }
                    showPicker(cal)
                } else {
                    thirdAlarm = null
                }
            }
        }

    }

}
