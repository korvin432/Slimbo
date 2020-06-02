package com.mindyapps.slimbo.ui.relax

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
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
        setUpPlayers()

        birds_card.setOnClickListener(this)
        cricket_card.setOnClickListener(this)
        nature_sounds_card.setOnClickListener(this)
        pink_noise_card.setOnClickListener(this)
        rain_in_a_car_card.setOnClickListener(this)
        rain_on_the_roof_card.setOnClickListener(this)
        street_noise_card.setOnClickListener(this)
        thunder_card.setOnClickListener(this)
        water_stream_card.setOnClickListener(this)
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

    private fun setUpPlayers() {
        birdsPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "birds", "raw", requireContext().packageName
            )
        )
        cricketPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "cricket", "raw", requireContext().packageName
            )
        )
        natureSoundsPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "nature_sounds", "raw", requireContext().packageName
            )
        )
        pinkNoisePlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "pink_noise", "raw", requireContext().packageName
            )
        )
        rainInCarPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "rain_in_a_car", "raw", requireContext().packageName
            )
        )
        rainOnTheRoofPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "rain_on_the_roof", "raw", requireContext().packageName
            )
        )
        streetNoisePlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "street_noise", "raw", requireContext().packageName
            )
        )
        thunderPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "thunder", "raw", requireContext().packageName
            )
        )
        waterStreamPlayer = MediaPlayer.create(
            context, requireContext().resources.getIdentifier(
                "water_stream", "raw", requireContext().packageName
            )
        )
    }

    private fun startPlaying(check: View, seekBar: SeekBar, resId: String, player: MediaPlayer) {
        val mediaPath = Uri.parse(
            "android.resource://" + requireContext().packageName + "/" + requireContext().resources.getIdentifier(
                resId, "raw", requireContext().packageName
            )
        )
        player.reset()
        player.setDataSource(requireContext(), mediaPath)
        player.prepare()
        player.isLooping = true
        player.start()
        check.visibility = View.VISIBLE
        seekBar.visibility = View.VISIBLE
        seekBar.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, progress: Int, b: Boolean) {
                val volume = (1 - ln((100 - progress).toDouble()) / ln(
                    100.toDouble()
                )).toFloat()
                player.setVolume(volume, volume)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {}
            override fun onStopTrackingTouch(seekBar: SeekBar) {}
        })
    }

    private fun endPlaying(check: View, seekBar: SeekBar, player: MediaPlayer) {
        check.visibility = View.GONE
        seekBar.visibility = View.GONE
        seekBar.progress = 100
        player.stop()
    }


    override fun onClick(view: View?) {
        when (view?.id) {
            R.id.birds_card -> {
                if (!birdsPlayer!!.isPlaying) {
                    startPlaying(birds_check, birds_seekbar, "birds", birdsPlayer!!)
                } else {
                    endPlaying(birds_check, birds_seekbar, birdsPlayer!!)
                }
            }
            R.id.cricket_card -> {
                if (!cricketPlayer!!.isPlaying) {
                    startPlaying(cricket_check, cricket_seekbar, "cricket", cricketPlayer!!)
                } else {
                    endPlaying(cricket_check, cricket_seekbar, cricketPlayer!!)
                }
            }
            R.id.nature_sounds_card -> {
                if (!natureSoundsPlayer!!.isPlaying) {
                    startPlaying(
                        nature_sounds_check,
                        nature_sounds_seekbar,
                        "nature_sounds",
                        natureSoundsPlayer!!
                    )
                } else {
                    endPlaying(nature_sounds_check, nature_sounds_seekbar, natureSoundsPlayer!!)
                }
            }
            R.id.pink_noise_card -> {
                if (!pinkNoisePlayer!!.isPlaying) {
                    startPlaying(
                        pink_noise_check,
                        pink_noise_seekbar,
                        "pink_noise",
                        pinkNoisePlayer!!
                    )
                } else {
                    endPlaying(pink_noise_check, pink_noise_seekbar, pinkNoisePlayer!!)
                }
            }
            R.id.rain_in_a_car_card -> {
                if (!rainInCarPlayer!!.isPlaying) {
                    startPlaying(
                        rain_in_a_car_check,
                        rain_in_a_car_seekbar,
                        "rain_in_a_car",
                        rainInCarPlayer!!
                    )
                } else {
                    endPlaying(rain_in_a_car_check, rain_in_a_car_seekbar, rainInCarPlayer!!)
                }
            }
            R.id.rain_on_the_roof_card -> {
                if (!rainOnTheRoofPlayer!!.isPlaying) {
                    startPlaying(
                        rain_on_the_roof_check,
                        rain_on_the_roof_seekbar,
                        "rain_on_the_roof",
                        rainOnTheRoofPlayer!!
                    )
                } else {
                    endPlaying(
                        rain_on_the_roof_check,
                        rain_on_the_roof_seekbar,
                        rainOnTheRoofPlayer!!
                    )
                }
            }
            R.id.street_noise_card -> {
                if (!streetNoisePlayer!!.isPlaying) {
                    startPlaying(
                        street_noise_check,
                        street_noise_seekbar,
                        "street_noise",
                        streetNoisePlayer!!
                    )
                } else {
                    endPlaying(street_noise_check, street_noise_seekbar, streetNoisePlayer!!)
                }
            }
            R.id.thunder_card -> {
                if (!thunderPlayer!!.isPlaying) {
                    startPlaying(thunder_check, thunder_seekbar, "thunder", thunderPlayer!!)
                } else {
                    endPlaying(thunder_check, thunder_seekbar, thunderPlayer!!)
                }
            }
            R.id.water_stream_card -> {
                if (!waterStreamPlayer!!.isPlaying) {
                    startPlaying(
                        water_stream_check,
                        water_stream_seekbar,
                        "water_stream",
                        waterStreamPlayer!!
                    )
                } else {
                    endPlaying(water_stream_check, water_stream_seekbar, waterStreamPlayer!!)
                }
            }
        }
    }
}
