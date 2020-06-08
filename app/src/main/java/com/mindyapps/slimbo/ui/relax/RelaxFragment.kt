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
    private lateinit var storagePath: File
    private lateinit var observerMusic: Observer<List<Music>>
    private var player: MediaPlayer? = null
    private var birdsPlayer: MediaPlayer? = null
    private var cricketPlayer: MediaPlayer? = null
    private var natureSoundsPlayer: MediaPlayer? = null
    private var pinkNoisePlayer: MediaPlayer? = null
    private var rainInCarPlayer: MediaPlayer? = null
    private var rainOnTheRoofPlayer: MediaPlayer? = null
    private var streetNoisePlayer: MediaPlayer? = null
    private var thunderPlayer: MediaPlayer? = null
    private var waterStreamPlayer: MediaPlayer? = null
    private var fanPlayer: MediaPlayer? = null
    private var forestPlayer: MediaPlayer? = null
    private var lightRainPlayer: MediaPlayer? = null
    private var mediumRainPlayer: MediaPlayer? = null
    private var rainPlayer: MediaPlayer? = null
    private var seaWavesPlayer: MediaPlayer? = null
    private var thunderAndWindPlayer: MediaPlayer? = null
    private var thunderRainLoadPlayer: MediaPlayer? = null

    private var root: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        storage = Firebase.storage

        storagePath = File(requireContext().externalCacheDir!!.absolutePath, "Music")
        if (!storagePath.exists()) {
            storagePath.mkdirs()
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (root == null) {
            viewModel = ViewModelProvider(
                this,
                RelaxViewModelFactory(repository, requireActivity().application)
            ).get(RelaxViewModel::class.java)

            root = inflater.inflate(R.layout.fragment_relax, container, false)

            recyclerView = root!!.findViewById(R.id.music_recycler)

        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setSubscriber()
        setUpPlayers()
        checkLoadedFiles()

        //free play buttons
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
        Log.d("qwwe", "onstop")
        if (player != null && player!!.isPlaying) player!!.stop()
        if (musicAdapter.mediaPlayer != null && musicAdapter.mediaPlayer!!.isPlaying) {
            musicAdapter.mediaPlayer!!.stop()
        }
        val mediaList: List<MediaPlayer?> = listOf(birdsPlayer, cricketPlayer, natureSoundsPlayer,
            pinkNoisePlayer, rainInCarPlayer, rainOnTheRoofPlayer, streetNoisePlayer, thunderPlayer,
            waterStreamPlayer, fanPlayer, forestPlayer, lightRainPlayer, mediumRainPlayer, rainPlayer,
            seaWavesPlayer, thunderAndWindPlayer, thunderRainLoadPlayer)
        mediaList.forEach {
            it?.stop()
        }
    }

    private fun checkLoadedFiles() {
        val storagePath = File(requireContext().externalCacheDir!!.absolutePath, "Music")
        if (!storagePath.exists()) storagePath.mkdirs()

        if (File(storagePath, "fan.mp3").exists()) {
            fan_load.visibility = View.GONE
            fan_card.setOnClickListener(this)
        } else {
            fan_card.setOnClickListener { downloadFile("fan.mp3") }
        }
        if (File(storagePath, "forest.mp3").exists()) {
            forest_load.visibility = View.GONE
            forest_card.setOnClickListener(this)
        } else {
            forest_card.setOnClickListener { downloadFile("forest.mp3") }
        }
        if (File(storagePath, "light_rain.mp3").exists()) {
            light_rain_load.visibility = View.GONE
            light_rain_card.setOnClickListener(this)
        } else {
            light_rain_card.setOnClickListener { downloadFile("light_rain.mp3") }
        }
        if (File(storagePath, "medium_rain.mp3").exists()) {
            medium_rain_load.visibility = View.GONE
            medium_rain_card.setOnClickListener(this)
        } else {
            medium_rain_card.setOnClickListener { downloadFile("medium_rain.mp3") }
        }
        if (File(storagePath, "rain.mp3").exists()) {
            rain_load.visibility = View.GONE
            rain_card.setOnClickListener(this)
        } else {
            rain_card.setOnClickListener { downloadFile("rain.mp3") }
        }
        if (File(storagePath, "sea_waves.mp3").exists()) {
            sea_waves_load.visibility = View.GONE
            sea_waves_card.setOnClickListener(this)
        } else {
            sea_waves_card.setOnClickListener { downloadFile("sea_waves.mp3") }
        }
        if (File(storagePath, "thunder_and_wind.mp3").exists()) {
            thunder_and_wind_load.visibility = View.GONE
            thunder_and_wind_card.setOnClickListener(this)
        } else {
            thunder_and_wind_card.setOnClickListener { downloadFile("thunder_and_wind.mp3") }
        }
        if (File(storagePath, "thunder_rain.mp3").exists()) {
            thunder_rain_load.visibility = View.GONE
            thunder_rain_card.setOnClickListener(this)
        } else {
            thunder_rain_card.setOnClickListener { downloadFile("thunder_rain.mp3") }
        }
    }

    private fun downloadFile(fileName: String) {
        Toast.makeText(requireContext(), getString(R.string.downloading), Toast.LENGTH_SHORT).show()
        val gsReference =
            storage.getReferenceFromUrl("gs://slimbo-9b6a7.appspot.com/$fileName")

        val myFile = File(storagePath, fileName)

        gsReference.getFile(myFile).addOnSuccessListener {
            setSubscriber()
            checkLoadedFiles()
            setUpPaidPlayers()
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
        setUpPaidPlayers()
    }

    private fun setUpPaidPlayers() {
        fanPlayer = MediaPlayer.create(context, Uri.parse(File(storagePath, "fan.mp3").path))
        forestPlayer = MediaPlayer.create(context, Uri.parse(File(storagePath, "forest.mp3").path))
        lightRainPlayer =
            MediaPlayer.create(context, Uri.parse(File(storagePath, "light_rain.mp3").path))
        mediumRainPlayer =
            MediaPlayer.create(context, Uri.parse(File(storagePath, "medium_rain.mp3").path))
        rainPlayer = MediaPlayer.create(context, Uri.parse(File(storagePath, "rain.mp3").path))
        seaWavesPlayer =
            MediaPlayer.create(context, Uri.parse(File(storagePath, "sea_waves.mp3").path))
        thunderAndWindPlayer =
            MediaPlayer.create(context, Uri.parse(File(storagePath, "thunder_and_wind.mp3").path))
        thunderRainLoadPlayer =
            MediaPlayer.create(context, Uri.parse(File(storagePath, "thunder_rain.mp3").path))
    }

    private fun startPlaying(check: View, seekBar: SeekBar, resId: String, player: MediaPlayer) {
        val mediaPath: Uri?

        mediaPath = if (!resId.contains(".mp3")) {
            Uri.parse(
                "android.resource://" + requireContext().packageName + "/" + requireContext().resources.getIdentifier(
                    resId, "raw", requireContext().packageName
                )
            )
        } else {
            val audioFile = File(storagePath, resId)
            Uri.parse(audioFile.path)
        }
        player.reset()
        player.setDataSource(requireContext(), mediaPath!!)
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
            R.id.fan_card -> {
                if (!fanPlayer!!.isPlaying) {
                    startPlaying(fan_check, fan_seekbar, "fan.mp3", fanPlayer!!)
                } else {
                    endPlaying(fan_check, fan_seekbar, fanPlayer!!)
                }
            }
            R.id.forest_card -> {
                if (!forestPlayer!!.isPlaying) {
                    startPlaying(forest_check, forest_seekbar, "forest.mp3", forestPlayer!!)
                } else {
                    endPlaying(forest_check, forest_seekbar, forestPlayer!!)
                }
            }
            R.id.light_rain_card -> {
                if (!lightRainPlayer!!.isPlaying) {
                    startPlaying(
                        light_rain_check,
                        light_rain_seekbar,
                        "light_rain.mp3",
                        lightRainPlayer!!
                    )
                } else {
                    endPlaying(light_rain_check, light_rain_seekbar, lightRainPlayer!!)
                }
            }
            R.id.medium_rain_card -> {
                if (!mediumRainPlayer!!.isPlaying) {
                    startPlaying(
                        medium_rain_check,
                        medium_rain_seekbar,
                        "medium_rain.mp3",
                        mediumRainPlayer!!
                    )
                } else {
                    endPlaying(medium_rain_check, medium_rain_seekbar, mediumRainPlayer!!)
                }
            }
            R.id.rain_card -> {
                if (!rainPlayer!!.isPlaying) {
                    startPlaying(rain_check, rain_seekbar, "rain.mp3", rainPlayer!!)
                } else {
                    endPlaying(rain_check, rain_seekbar, rainPlayer!!)
                }
            }
            R.id.sea_waves_card -> {
                if (!seaWavesPlayer!!.isPlaying) {
                    startPlaying(
                        sea_waves_check,
                        sea_waves_seekbar,
                        "sea_waves.mp3",
                        seaWavesPlayer!!
                    )
                } else {
                    endPlaying(sea_waves_check, sea_waves_seekbar, seaWavesPlayer!!)
                }
            }
            R.id.thunder_and_wind_card -> {
                if (!thunderAndWindPlayer!!.isPlaying) {
                    startPlaying(
                        thunder_and_wind_check,
                        thunder_and_wind_seekbar,
                        "thunder_and_wind.mp3",
                        thunderAndWindPlayer!!
                    )
                } else {
                    endPlaying(
                        thunder_and_wind_check,
                        thunder_and_wind_seekbar,
                        thunderAndWindPlayer!!
                    )
                }
            }
            R.id.thunder_rain_card -> {
                if (!thunderRainLoadPlayer!!.isPlaying) {
                    startPlaying(
                        thunder_rain_check,
                        thunder_rain_seekbar,
                        "thunder_rain.mp3",
                        thunderRainLoadPlayer!!
                    )
                } else {
                    endPlaying(
                        thunder_rain_check,
                        thunder_rain_seekbar,
                        thunderRainLoadPlayer!!
                    )
                }
            }
        }
    }
}
