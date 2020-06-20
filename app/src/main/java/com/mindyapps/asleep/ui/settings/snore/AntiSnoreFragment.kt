package com.mindyapps.asleep.ui.settings.snore

import android.content.Intent
import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.mindyapps.asleep.MainActivity
import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Music
import com.mindyapps.asleep.data.repository.SlimboRepositoryImpl
import com.mindyapps.asleep.preferences.SleepingStore
import com.mindyapps.asleep.ui.adapters.SelectMusicAdapter
import com.mindyapps.asleep.ui.subs.SubscribeActivity
import com.xw.repo.BubbleSeekBar
import kotlinx.android.synthetic.main.anti_snore_fragment.*
import kotlinx.coroutines.launch
import java.io.File

class AntiSnoreFragment : Fragment(), BubbleSeekBar.OnProgressChangedListener {

    private lateinit var viewModel: AntiSnoreViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var durationSeekBar: BubbleSeekBar
    private lateinit var selectedMusicAdapter: SelectMusicAdapter
    private lateinit var observerMusic: Observer<List<Music>>
    private lateinit var snoreSwitch: Switch
    private lateinit var vibrationSwitch: Switch
    private lateinit var sleepingStore: SleepingStore
    private lateinit var storage: FirebaseStorage

    private var selectedSignal: Music? = null
    private var repository = SlimboRepositoryImpl()
    private var player: MediaPlayer? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.anti_snore_fragment, container, false)
        viewModel = ViewModelProvider(this,
            AntiSnoreViewModelFactory(repository, requireActivity().application)
        ).get(AntiSnoreViewModel::class.java)

        setHasOptionsMenu(true)
        storage = Firebase.storage

        recyclerView = root.findViewById(R.id.alarm_recycler)
        durationSeekBar = root.findViewById(R.id.duration_seekbar)
        snoreSwitch = root.findViewById(R.id.snore_switch)
        vibrationSwitch = root.findViewById(R.id.vibration_switch)
        sleepingStore = SleepingStore(
            PreferenceManager.getDefaultSharedPreferences
                (requireActivity().applicationContext)
        )

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        snoreSwitch.setOnCheckedChangeListener { compoundButton, b ->
            lifecycleScope.launch {
                findNavController().previousBackStackEntry?.savedStateHandle?.set(
                    "use_anti_snore",
                    snoreSwitch.isChecked
                )
            }
        }
        vibrationSwitch.setOnCheckedChangeListener { button, b ->
            if (button.isChecked) {
                sleepingStore.useVibration = true
                sound_container.visibility = View.GONE
            } else {
                sleepingStore.useVibration = false
                sound_container.visibility = View.VISIBLE
            }
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        loadPreferences()
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

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.anti_snore_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.about) {
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            with(builder)
            {
                setTitle(getString(R.string.anti_snore))
                setMessage(getString(R.string.anti_snore_tip))
                show()
            }
            return true
        }
        return NavigationUI.onNavDestinationSelected(
            item,
            requireView().findNavController()
        ) || super.onOptionsItemSelected(item)
    }

    override fun onDetach() {
        super.onDetach()
        stopPlaying()
        savePreferences()
    }

    private fun loadPreferences() {
        snoreSwitch.isChecked = sleepingStore.useAntiSnore
        vibrationSwitch.isChecked = sleepingStore.useVibration
        selectedSignal = Gson().fromJson(sleepingStore.antiSnoreSound, Music::class.java)
        durationSeekBar.setProgress(sleepingStore.antiSnoreDuration.toFloat())
        durationSeekBar.onProgressChangedListener = this
    }

    private fun savePreferences() {
        sleepingStore.useAntiSnore = snoreSwitch.isChecked
        sleepingStore.antiSnoreSound = Gson().toJson(selectedSignal)
        sleepingStore.antiSnoreDuration = durationSeekBar.progress
    }

    private fun downloadFile(fileName: String) {
        Toast.makeText(requireContext(), getString(R.string.downloading), Toast.LENGTH_SHORT).show()
        val gsReference =
            storage.getReferenceFromUrl("gs://asleep-ed29b.appspot.com/$fileName")

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

    private fun loadMusic() {
        lifecycleScope.launch {
            viewModel.allAlarms.observe(viewLifecycleOwner, observerMusic)
        }
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
        selectedMusicAdapter = SelectMusicAdapter(
            musicList.toMutableList(), selectedSignal, requireActivity().applicationContext
        )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = selectedMusicAdapter
        selectedMusicAdapter.onItemClick = { music ->
            stopPlaying()
            if (!music.free!!) {
                val subscribed = (requireActivity() as MainActivity).subscribed
                if (subscribed) {
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
                    val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
                    with(builder)
                    {
                        setTitle(getString(R.string.subscribe))
                        setMessage(getString(R.string.get_all_music))
                        setOnDismissListener {
                            startActivity(Intent(requireContext(), SubscribeActivity::class.java))
                        }
                        show()
                    }
                }
            } else {
                val resID =
                    resources.getIdentifier(music.fileName, "raw", requireContext().packageName)
                player = MediaPlayer.create(requireContext(), resID)
                player!!.isLooping = true
                player!!.start()
                selectedSignal = music
            }
        }
    }

    override fun onProgressChanged(
        bubbleSeekBar: BubbleSeekBar?,
        progress: Int,
        progressFloat: Float,
        fromUser: Boolean
    ) {
        sleepingStore.antiSnoreDuration = progress * 1000
    }

    override fun getProgressOnActionUp(
        bubbleSeekBar: BubbleSeekBar?,
        progress: Int,
        progressFloat: Float
    ) {
    }

    override fun getProgressOnFinally(
        bubbleSeekBar: BubbleSeekBar?,
        progress: Int,
        progressFloat: Float,
        fromUser: Boolean
    ) {
    }

}
