package com.mindyapps.asleep.ui.sleep

import android.Manifest.permission.*
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.mindyapps.asleep.MainActivity
import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Factor
import com.mindyapps.asleep.data.model.Music
import com.mindyapps.asleep.preferences.AlarmStore
import com.mindyapps.asleep.preferences.SleepingStore
import com.mindyapps.asleep.preferences.SleepingStore.Companion.USE_ANTI_SNORE
import com.mindyapps.asleep.ui.adapters.SelectedFactorsRecyclerAdapter
import com.mindyapps.asleep.ui.sleeping.SleepingActivity
import kotlinx.android.synthetic.main.fragment_sleep.*


class SleepFragment : Fragment(), View.OnClickListener {

    private lateinit var sleepViewModel: SleepViewModel
    private lateinit var factorsCard: CardView
    private lateinit var musicCard: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var startButton: MaterialButton
    private lateinit var alarmChip: Chip
    private lateinit var snoreChip: Chip
    private lateinit var alarmStore: AlarmStore
    private lateinit var sleepingStore: SleepingStore
    private lateinit var selectedFactorsRecyclerAdapter: SelectedFactorsRecyclerAdapter
    private var selectedFactors: ArrayList<Factor>? = ArrayList()
    private var selectedMusic: Music? = null
    private var selectedLength: String? = null
    private var alarmChipText: String? = null
    private var useAlarm: Boolean? = null
    private var useAntiSnore: Boolean? = null
    private lateinit var preferences: SharedPreferences

    private var root: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext())
        preferences.registerOnSharedPreferenceChangeListener(listener)
        if ((activity as MainActivity?)!!.recording != null) {
            val bundle = bundleOf(
                "recording" to ((activity as MainActivity?)!!.recording)
            )
            findNavController().navigate(R.id.recordingFragment, bundle)
        }
    }

    private var listener: SharedPreferences.OnSharedPreferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == AlarmStore.USE_ALARM || key == AlarmStore.ALARM_TIME){
                if (!alarmStore.useAlarm) {
                    alarmChip.text = getString(R.string.off)
                } else {
                    alarmChip.text = alarmStore.alarmTime
                }
            }
            if (key == USE_ANTI_SNORE){
                if (!sleepingStore.useAntiSnore) {
                    snoreChip.text = getString(R.string.off)
                } else {
                    snoreChip.text = getString(R.string.on)
                }
            }
        }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_sleep, container, false)
            sleepViewModel =
                ViewModelProvider(this, SleepViewModelFactory()).get(SleepViewModel::class.java)
            val loginActivityBackground: Drawable =
                root!!.findViewById<RelativeLayout>(R.id.sleep_layout).background
            loginActivityBackground.alpha = 40

            factorsCard = root!!.findViewById(R.id.factors_card)
            factorsCard.background.alpha = 20
            musicCard = root!!.findViewById(R.id.sound_card)
            musicCard.background.alpha = 20
            recyclerView = root!!.findViewById(R.id.selected_factors_recycler)
            startButton = root!!.findViewById(R.id.start_sleeping_button)
            alarmChip = root!!.findViewById(R.id.alarm_chip)
            snoreChip = root!!.findViewById(R.id.snore_chip)
            alarmStore = AlarmStore(
                PreferenceManager.getDefaultSharedPreferences
                    (requireActivity().applicationContext)
            )
            sleepingStore = SleepingStore(
                PreferenceManager.getDefaultSharedPreferences
                    (requireActivity().applicationContext)
            )

            if (alarmStore.useAlarm) {
                alarmChip.text = alarmStore.alarmTime
            }

            if (sleepingStore.useAntiSnore) {
                snoreChip.text = getString(R.string.on)
            }

            factorsCard.setOnClickListener(this)
            musicCard.setOnClickListener(this)
            startButton.setOnClickListener(this)
            alarmChip.setOnClickListener(this)
            snoreChip.setOnClickListener(this)

        }

        return root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<Factor>>(
            "factors"
        )?.observe(viewLifecycleOwner) { result ->
            selectedFactors = result
            bindFactorsRecycler(result)
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Music>(
            "selected_music"
        )?.observe(viewLifecycleOwner) { result ->
            selectedMusic = result
            if (result.name != requireContext().getString(R.string.do_not_use)) {
                selected_music_textview.text = selectedMusic!!.name
                selected_music_textview.visibility = View.VISIBLE
            } else {
                selected_music_textview.visibility = View.GONE
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "selected_length"
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                selectedLength = result
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            "use_alarm"
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                useAlarm = result
                if (!useAlarm!!) {
                    alarmChip.text = getString(R.string.off)
                } else {
                    alarmChip.text = alarmChipText
                }
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>(
            "alarm_time"
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                try {
                    if (useAlarm!!) {
                        alarmChipText = result
                        alarmChip.text = alarmChipText
                    } else {
                        alarmChip.text = getString(R.string.off)
                    }
                } catch (ex: Exception) {
                }
            }
        }


        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Boolean>(
            "use_anti_snore"
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                useAntiSnore = result
                if (!useAntiSnore!!) {
                    snoreChip.text = getString(R.string.off)
                } else {
                    snoreChip.text = getString(R.string.on)
                }
            }
        }
    }

    private fun bindFactorsRecycler(factors: ArrayList<Factor>) {
        selectedFactorsRecyclerAdapter =
            SelectedFactorsRecyclerAdapter(factors, requireActivity().applicationContext)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 6)
        recyclerView.adapter = selectedFactorsRecyclerAdapter
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.factors_card -> {
                if (selectedFactors!!.size > 0) {
                    val bundle = bundleOf("selected_factors" to selectedFactors!!.toTypedArray())
                    findNavController().navigate(R.id.factors_dialog, bundle)
                } else {
                    findNavController().navigate(R.id.factors_dialog)
                }
            }
            R.id.sound_card -> {
                if (selectedMusic != null) {
                    val bundle = bundleOf(
                        "selected_music" to selectedMusic,
                        "selected_length" to selectedLength
                    )
                    findNavController().navigate(R.id.select_music_dialog, bundle)
                } else {
                    findNavController().navigate(R.id.select_music_dialog)
                }
            }
            R.id.alarm_chip -> {
                findNavController().navigate(R.id.alarmSettingsFragment)
            }
            R.id.snore_chip -> {
                findNavController().navigate(R.id.antiSnoreFragment)
            }
            R.id.start_sleeping_button -> {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        WRITE_EXTERNAL_STORAGE
                    ) != PackageManager.PERMISSION_GRANTED || ContextCompat.checkSelfPermission(
                        requireContext(),
                        RECORD_AUDIO
                    ) != PackageManager.PERMISSION_GRANTED
                ) {
                    ActivityCompat.requestPermissions(
                        requireActivity(),
                        arrayOf(WRITE_EXTERNAL_STORAGE, READ_EXTERNAL_STORAGE, WAKE_LOCK,
                            RECEIVE_BOOT_COMPLETED, RECORD_AUDIO, FOREGROUND_SERVICE),
                        12
                    )
                }
                    //Do the stuff that requires permission...
                    if (SleepingStore(PreferenceManager.getDefaultSharedPreferences(requireContext())).showTip) {
                        val bundle = bundleOf(
                            "selected_music" to selectedMusic,
                            "selected_length" to selectedLength,
                            "selected_factors" to selectedFactors!!
                        )
                        findNavController().navigate(R.id.sleeping_tip_fragment, bundle)
                    } else {
                        val intent = Intent(requireContext(), SleepingActivity::class.java)
                        intent.putExtra("music", selectedMusic)
                        intent.putExtra("duration", selectedLength)
                        intent.putExtra("factors", selectedFactors)
                        startActivity(intent)
                    }

            }
        }
    }
}
