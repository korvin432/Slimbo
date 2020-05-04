package com.mindyapps.android.slimbo.ui.music_select

import android.media.MediaPlayer
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.ui.adapters.SelectedMusicAdapter
import kotlinx.coroutines.launch
import java.util.concurrent.TimeUnit


class SelectMusicFragment : DialogFragment() {

    private lateinit var viewModel: SelectMusicViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: MaterialButton
    private lateinit var selectedMusicAdapter: SelectedMusicAdapter

    private val sourceList = ArrayList<Music>()
    private var selectedMusic: Music? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.select_music_fragment, container, false)
        this.dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (requireArguments().getParcelable<Music>("selected_music") != null) {
            selectedMusic = requireArguments().getParcelable<Music>("selected_music")
        }

        recyclerView = root.findViewById(R.id.music_recycler)
        confirmButton = root.findViewById(R.id.confirm_music_button)
        confirmButton.setOnClickListener {
            if (selectedMusic != null) {
                lifecycleScope.launch {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_music",
                        selectedMusic
                    )
                }
                dismiss()
            }
        }

        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SelectMusicViewModel::class.java)

        populateSourceList()
        bindRecyclerView()

    }

    private fun populateSourceList() {
        sourceList.add(
            Music(
                null, "Autumn dream lullaby", "autumn_dream_lullaby",
                getDurationLength(R.raw.autumn_dream_lullaby)
            )
        )
        sourceList.add(
            Music(
                null, "Back to the Night", "back_to_the_night",
                getDurationLength(R.raw.back_to_the_night)
            )
        )
        sourceList.add(
            Music(
                null, "Deepblue", "deepblue",
                getDurationLength(R.raw.back_to_the_night)
            )
        )
        sourceList.add(
            Music(
                null, "Looking at the night sky", "looking_at_the_night_sky",
                getDurationLength(R.raw.looking_at_the_night_sky)
            )
        )
        sourceList.add(
            Music(
                null, "Mystic sea", "mystic_sea",
                getDurationLength(R.raw.mystic_sea)
            )
        )
        sourceList.add(
            Music(
                null, "New dawn", "new_dawn",
                getDurationLength(R.raw.new_dawn)
            )
        )
        sourceList.add(
            Music(
                null, "Ofelias Dream", "ofelias_dream",
                getDurationLength(R.raw.ofelias_dream)
            )
        )
        sourceList.add(
            Music(
                null, "Relaxing", "relaxing",
                getDurationLength(R.raw.relaxing)
            )
        )
        sourceList.add(
            Music(
                null, "Renovation", "renovation",
                getDurationLength(R.raw.renovation)
            )
        )
        sourceList.add(
            Music(
                null, "The House Glows", "the_house_glows",
                getDurationLength(R.raw.the_house_glows)
            )
        )
    }

    private fun getDurationLength(id: Int): String {
        val mp: MediaPlayer = MediaPlayer.create(context, id)
        return String.format(
            "%d:%d",
            TimeUnit.MILLISECONDS.toMinutes(mp.duration.toLong()),
            TimeUnit.MILLISECONDS.toSeconds(mp.duration.toLong()) -
                    TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(mp.duration.toLong()))
        )
    }

    private fun bindRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        val dividerItemDecoration = DividerItemDecoration(
            recyclerView.context,
            linearLayoutManager.orientation
        )
        recyclerView.addItemDecoration(dividerItemDecoration)
        selectedMusicAdapter =
            SelectedMusicAdapter(
                sourceList.toMutableList(),
                selectedMusic,
                requireActivity().applicationContext
            )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = selectedMusicAdapter
        selectedMusicAdapter.onItemClick = { music ->
//            val resID =
//                resources.getIdentifier(music.fileName, "raw", getPackageName())
//
//            val mediaPlayer: MediaPlayer = MediaPlayer.create(requireContext(), resID)
//            mediaPlayer.start()
            selectedMusic = music
        }
    }

}
