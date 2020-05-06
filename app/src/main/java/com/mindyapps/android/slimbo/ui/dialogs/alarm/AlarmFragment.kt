package com.mindyapps.android.slimbo.ui.dialogs.alarm

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.TimePicker
import android.widget.Toast
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
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.android.slimbo.ui.adapters.SelectedMusicAdapter
import com.mindyapps.android.slimbo.ui.dialogs.music_select.SelectMusicViewModel
import com.mindyapps.android.slimbo.ui.dialogs.music_select.SelectMusicViewModelFactory
import kotlinx.coroutines.launch

class AlarmFragment : DialogFragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: AlarmViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: MaterialButton
    private lateinit var timePicker: TimePicker
    private lateinit var selectedMusicAdapter: SelectedMusicAdapter

    private lateinit var observerMusic: Observer<List<Music>>
    private var selectedAlarm: Music? = null
    private var selectedHour: Int? = null
    private var selectedMinutes: Int? = null
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

        if (requireArguments().getParcelable<Music>("selected_alarm_sound") != null) {
            selectedAlarm = requireArguments().getParcelable("selected_alarm_sound")
            selectedHour = requireArguments().getInt("selected_hour")
            selectedMinutes = requireArguments().getInt("selected_minutes")
        }

        recyclerView = root.findViewById(R.id.alarm_recycler)
        confirmButton = root.findViewById(R.id.confirm_alarm_button)
        timePicker = root.findViewById(R.id.timePicker)
        timePicker.setIs24HourView(DateFormat.is24HourFormat(requireContext()))

        timePicker.setOnTimeChangedListener { _, hour, minute ->
            moved = true
            selectedHour = hour
            selectedMinutes = minute
        }

        confirmButton.setOnClickListener {
            if (selectedAlarm != null && moved) {
                lifecycleScope.launch {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_hour", selectedHour
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_minutes", selectedMinutes
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_alarm_sound", selectedAlarm
                    )
                }
                dismiss()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please, select all data or dismiss the dialog",
                    Toast.LENGTH_SHORT
                ).show()
            }
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

}
