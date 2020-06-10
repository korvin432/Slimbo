package com.mindyapps.slimbo.ui.dialogs.music_select

import android.content.DialogInterface
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
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
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.ui.adapters.SelectMusicAdapter
import kotlinx.coroutines.launch
import org.angmarch.views.NiceSpinner
import java.io.File
import java.util.*


class SelectMusicFragment : DialogFragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: SelectMusicViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: MaterialButton
    private lateinit var selectedMusicAdapter: SelectMusicAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var observerMusic: Observer<List<Music>>
    private var selectedMusic: Music? = null
    private var selectedLength: String? = null
    var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.select_music_fragment, container, false)
        this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)
        viewModel = ViewModelProvider(
            this,
            SelectMusicViewModelFactory(repository, requireActivity().application)
        ).get(SelectMusicViewModel::class.java)

        if (requireArguments().getParcelable<Music>("selected_music") != null) {
            selectedMusic = requireArguments().getParcelable("selected_music")
            selectedLength = requireArguments().getString("selected_length")
        }

        recyclerView = root.findViewById(R.id.music_recycler)
        confirmButton = root.findViewById(R.id.confirm_music_button)

        val niceSpinner = root.findViewById(R.id.nice_spinner) as NiceSpinner
        val dataset: List<String> =
            LinkedList(
                listOf(
                    requireContext().resources.getString(R.string.five_minutes),
                    requireContext().resources.getString(R.string.ten_minutes),
                    requireContext().resources.getString(R.string.twenty_minutes),
                    requireContext().resources.getString(R.string.thirty_minutes),
                    requireContext().resources.getString(R.string.ffive_minutes)
                )
            )
        niceSpinner.attachDataSource(dataset)

        for (i in dataset.indices) {
            if (dataset[i] == selectedLength) {
                niceSpinner.selectedIndex = i
            }
        }

        confirmButton.setOnClickListener {
            if (selectedMusic != null) {
                lifecycleScope.launch {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_music",
                        selectedMusic
                    )
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "selected_length",
                        niceSpinner.text.toString()
                    )
                }
                dismiss()
            }
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setSubscriber()
    }

    private fun setSubscriber() {
        observerMusic = Observer { newsSource ->
            if (newsSource.isNotEmpty()) {
                bindRecyclerView(newsSource)
            }
        }
        loadMusic()
    }

    private fun loadMusic() {
        lifecycleScope.launch {
            if (view != null) {
                viewModel.allMusic.observe(viewLifecycleOwner, observerMusic)
            }
        }
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)
        stopPlaying()
    }

    private fun stopPlaying() {
        if (player != null && player!!.isPlaying) player!!.stop()
    }

    private fun downloadFile(fileName: String) {
        Toast.makeText(requireContext(), getString(R.string.downloading), Toast.LENGTH_SHORT).show()
        val gsReference =
            storage.getReferenceFromUrl("gs://slimbo-9b6a7.appspot.com/$fileName")

        val storagePath = File(requireContext().externalCacheDir!!.absolutePath, "Music")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

        val myFile = File(storagePath, fileName)

        gsReference.getFile(myFile).addOnSuccessListener {
            setSubscriber()
        }.addOnFailureListener {
            Toast.makeText(requireContext(), getString(R.string.error), Toast.LENGTH_SHORT).show()
        }
    }

    private fun bindRecyclerView(musicList: List<Music>) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(musicList.size)
        selectedMusicAdapter =
            SelectMusicAdapter(
                musicList.toMutableList(),
                selectedMusic,
                requireActivity().applicationContext
            )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = selectedMusicAdapter
        selectedMusicAdapter.onItemClick = { music ->
            stopPlaying()
            if (music.name != requireContext().getString(R.string.do_not_use)) {
                if (!music.free!!) {
                    val storagePath =
                        File(requireContext().externalCacheDir!!.absolutePath, "Music")
                    if (!storagePath.exists()) {
                        storagePath.mkdirs()
                    }
                    val audioFile = File(storagePath, "${music.fileName}.mp3")
                    if (!audioFile.exists()) {
                        downloadFile("${music.fileName}.mp3")
                    } else {
                        player = MediaPlayer.create(requireContext(), Uri.parse(audioFile.path))
                        player!!.start()
                    }
                } else {
                    val resID =
                        resources.getIdentifier(music.fileName, "raw", requireContext().packageName)
                    player = MediaPlayer.create(requireContext(), resID)
                    player!!.start()
                }
            }
            selectedMusic = music
        }
    }

}
