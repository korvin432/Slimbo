package com.mindyapps.slimbo.ui.relax

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.SeekBar.OnSeekBarChangeListener
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.ui.adapters.RelaxMusicAdapter
import kotlinx.android.synthetic.main.fragment_relax.*
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import kotlin.math.ln


class RelaxFragment : Fragment(), View.OnClickListener {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: RelaxViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicAdapter: RelaxMusicAdapter
    private lateinit var storage: FirebaseStorage
    private lateinit var observerMusic: Observer<List<Music>>
    var player: MediaPlayer? = null

    var birdsPlayer: MediaPlayer? = null
    var cricketPlayer: MediaPlayer? = null
    var natureSoundsPlayer: MediaPlayer? = null
    var pinkNoisePlayer: MediaPlayer? = null
    var rainInCarPlayer: MediaPlayer? = null
    var rainOnTheRoofPlayer: MediaPlayer? = null
    var streetNoisePlayer: MediaPlayer? = null
    var thunderPlayer: MediaPlayer? = null
    var waterStreamPlayer: MediaPlayer? = null

    var playersList: List<MediaPlayer> = LinkedList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            RelaxViewModelFactory(repository, requireActivity().application)
        ).get(RelaxViewModel::class.java)

        val root = inflater.inflate(R.layout.fragment_relax, container, false)

        recyclerView = root.findViewById(R.id.music_recycler)

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSubscriber()

        birds_card.setOnClickListener(this)
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
            viewModel.allMusic.observe(viewLifecycleOwner, observerMusic)
        }
    }

    override fun onStop() {
        super.onStop()
        stopPlaying()
    }

    private fun stopPlaying() {
        if (player != null && player!!.isPlaying) player!!.stop()
        if (musicAdapter.mediaPlayer != null && musicAdapter.mediaPlayer!!.isPlaying) {
            musicAdapter.mediaPlayer!!.stop()
        }
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
        musicAdapter =
            RelaxMusicAdapter(musicList.toMutableList(), requireActivity().applicationContext)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = musicAdapter
        musicAdapter.onItemClick = { music ->
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
                }
            }
        }
    }

    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.birds_card -> {
                if (birdsPlayer == null) {
                    val resID = requireContext().resources.getIdentifier(
                        "birds", "raw", requireContext().packageName
                    )
                    birdsPlayer = MediaPlayer.create(context, resID)
                    birdsPlayer!!.isLooping = true
                    birdsPlayer!!.start()

                    birds_check.visibility = View.VISIBLE
                    birds_seekbar.visibility = View.VISIBLE

                    birds_seekbar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
                        override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                            val volume = (1 - ln((100 - progress).toDouble()) / ln(
                                    100.toDouble())).toFloat()
                            birdsPlayer!!.setVolume(volume, volume)
                        }
                        override fun onStartTrackingTouch(seekBar: SeekBar) {}
                        override fun onStopTrackingTouch(seekBar: SeekBar) {}
                    })

                } else {
                    birds_check.visibility = View.GONE
                    birds_seekbar.visibility = View.GONE
                    birds_seekbar.progress = 100
                    birdsPlayer!!.stop()
                    birdsPlayer = null
                }

            }
        }
    }
}
