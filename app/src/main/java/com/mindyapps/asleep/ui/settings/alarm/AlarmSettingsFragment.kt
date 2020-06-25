package com.mindyapps.asleep.ui.settings.alarm

import android.Manifest.permission.SET_ALARM
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.format.DateFormat
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.mindyapps.asleep.MainActivity
import com.mindyapps.asleep.receivers.AlarmReceiver

import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Music
import com.mindyapps.asleep.data.repository.SlimboRepositoryImpl
import com.mindyapps.asleep.preferences.AlarmStore
import com.mindyapps.asleep.ui.adapters.SelectMusicAdapter
import com.mindyapps.asleep.ui.subs.SubscribeActivity
import kotlinx.android.synthetic.main.fragment_alarm_settings.*
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class AlarmSettingsFragment : Fragment(), CompoundButton.OnCheckedChangeListener {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: AlarmViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var selectedMusicAdapter: SelectMusicAdapter
    private lateinit var timePicker: TimePicker
    private lateinit var alarmSwitch: Switch
    private lateinit var smartAlarmSwitch: Switch
    private lateinit var repeatSpinner: Spinner
    private lateinit var alarmStore: AlarmStore
    private lateinit var storage: FirebaseStorage

    private lateinit var observerMusic: Observer<List<Music>>
    private var AM_PM: String = ""
    private var selectedAlarm: Music? = null
    private var selectedTime: String = "00:00"
    private var selectedSmartTime: String = ""
    private var selectedDays: ArrayList<Int> = ArrayList()
    var player: MediaPlayer? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.fragment_alarm_settings, container, false)
        viewModel = ViewModelProvider(
            this, AlarmViewModelFactory(
                repository, requireActivity().application
            )
        ).get(AlarmViewModel::class.java)
        storage = Firebase.storage

        recyclerView = root.findViewById(R.id.alarm_recycler)
        timePicker = root.findViewById(R.id.alarm_time_picker)
        alarmSwitch = root.findViewById(R.id.alarm_switch)
        smartAlarmSwitch = root.findViewById(R.id.smart_alarm_switch)
        repeatSpinner = root.findViewById(R.id.repeat_spinner)
        alarmStore = AlarmStore(
            PreferenceManager.getDefaultSharedPreferences
                (requireActivity().applicationContext)
        )

        timePicker.setIs24HourView(DateFormat.is24HourFormat(requireContext()))
        val minutes = resources.getStringArray(R.array.repeat_minutes)
        repeatSpinner.adapter =
            ArrayAdapter<String>(requireContext(), R.layout.spinner_item, minutes)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        monCheckBox.setOnCheckedChangeListener(this)
        tueCheckBox.setOnCheckedChangeListener(this)
        wedCheckBox.setOnCheckedChangeListener(this)
        thuCheckBox.setOnCheckedChangeListener(this)
        friCheckBox.setOnCheckedChangeListener(this)
        sunCheckBox.setOnCheckedChangeListener(this)
        satCheckBox.setOnCheckedChangeListener(this)
        alarmSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (ContextCompat.checkSelfPermission(requireContext(), SET_ALARM)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(requireActivity(), arrayOf(SET_ALARM), 12)
            } else {
                lifecycleScope.launch {
                    findNavController().previousBackStackEntry?.savedStateHandle?.set(
                        "use_alarm",
                        alarmSwitch.isChecked
                    )
                }
            }
        }
        timePicker.setOnTimeChangedListener { b, hour, min ->
            AM_PM = if (hour < 12) {
                "AM"
            } else {
                "PM"
            }
            selectedTime = String.format("%02d:%02d", hour, min)
            if (!b.is24HourView) {
                val _24HourSDF = SimpleDateFormat("HH:mm")
                val _12HourSDF = SimpleDateFormat("hh:mm a")
                val _24HourDt = _24HourSDF.parse(selectedTime)
                selectedTime = _12HourSDF.format(_24HourDt)
            }
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "alarm_time",
                selectedTime
            )
        }
        smartAlarmSwitch.setOnCheckedChangeListener { compoundButton, b ->
            if (smartAlarmSwitch.isChecked) {
                val dateFormat = if (!DateFormat.is24HourFormat(requireContext())) {
                    SimpleDateFormat("HH:mm a")
                } else {
                    SimpleDateFormat("HH:mm")
                }
                val date = dateFormat.parse(selectedTime)
                val minusDate = Date(date.time - (0..1800000).random())
                selectedSmartTime = dateFormat.format(minusDate)
                alarmStore.smartAlarmTime = selectedSmartTime
            } else {
                selectedSmartTime = ""
                alarmStore.smartAlarmTime = ""
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

    override fun onDetach() {
        super.onDetach()
        if (alarmSwitch.isChecked) {
            if (!smartAlarmSwitch.isChecked) {
                setAlarm(selectedTime)
            } else {
                setAlarm(selectedSmartTime)
            }
        }
        stopPlaying()
        savePreferences()
    }

    private fun setAlarm(time: String) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        selectedDays.forEach {
            val calendar = Calendar.getInstance()
            calendar.set(Calendar.DAY_OF_WEEK, it + 1)
            if (DateFormat.is24HourFormat(requireContext())) {
                calendar.set(Calendar.HOUR_OF_DAY, time.substringBefore(":").toInt())
            } else {
                calendar.set(Calendar.HOUR, time.substringBefore(":").toInt())
                if (AM_PM == "AM") {
                    calendar.set(Calendar.AM_PM, 0)
                } else {
                    calendar.set(Calendar.AM_PM, 1)
                }
            }
            val min = time.substringBefore(" ")
            calendar.set(Calendar.MINUTE, min.substringAfter(":").toInt())
            if (Calendar.getInstance().timeInMillis >= calendar.timeInMillis) {
                calendar.add(Calendar.DATE, 7)
            }
            val intent = Intent(requireContext(), AlarmReceiver::class.java)
            val pendingIntent = PendingIntent.getBroadcast(requireContext(), it + 1, intent, 0)
            alarmManager.setRepeating(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                24 * 7 * 60 * 60 * 1000,
                pendingIntent
            )

        }

    }


    private fun loadPreferences() {
        alarmSwitch.isChecked = alarmStore.useAlarm
        repeatSpinner.setSelection(alarmStore.repeatMinutes)
        smartAlarmSwitch.isChecked = alarmStore.smartAlarmTime != ""
        if (alarmStore.alarmTime != "00:00") {
            try {
                if (!DateFormat.is24HourFormat(requireContext())) {
                    val h_mm_a = SimpleDateFormat("hh:mm a")
                    val hh_mm_ss = SimpleDateFormat("HH:mm")
                    val d1 = h_mm_a.parse(alarmStore.alarmTime)
                    val time24 = hh_mm_ss.format(d1)
                    timePicker.currentHour = (time24).substringBefore(":").toInt()
                    timePicker.currentMinute = (time24).substringAfter(":").toInt()
                } else {
                    timePicker.currentHour =
                        (alarmStore.alarmTime)
                            .replace("AM", "").replace("PM", "")
                            .trim().substringBefore(":").toInt()
                    timePicker.currentMinute =
                        (alarmStore.alarmTime)
                            .replace("AM", "").replace("PM", "")
                            .trim().substringAfter(":").toInt()
                }
            } catch(ex: Exception){

            }
        }
        stringToWords(alarmStore.repeatDays).forEach {
            when (it.trim()) {
                "1" -> monCheckBox.isChecked = true
                "2" -> tueCheckBox.isChecked = true
                "3" -> wedCheckBox.isChecked = true
                "4" -> thuCheckBox.isChecked = true
                "5" -> friCheckBox.isChecked = true
                "6" -> satCheckBox.isChecked = true
                "7" -> sunCheckBox.isChecked = true
            }
        }
        selectedAlarm = Gson().fromJson(alarmStore.alarmSound, Music::class.java)
    }

    private fun savePreferences() {
        alarmStore.useAlarm = alarmSwitch.isChecked
        alarmStore.alarmTime = selectedTime
        alarmStore.smartAlarmTime = selectedSmartTime
        alarmStore.alarmSound = Gson().toJson(selectedAlarm)
        alarmStore.repeatDays = selectedDays.toString().replace("[", "").replace("]", "")
        alarmStore.repeatMinutes = repeatSpinner.selectedItemPosition
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

    private fun bindRecyclerView(musicList: List<Music>) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recyclerView.addItemDecoration(
            DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL)
        )
        recyclerView.setHasFixedSize(true)
        recyclerView.setItemViewCacheSize(musicList.size)
        selectedMusicAdapter = SelectMusicAdapter(
            musicList.toMutableList(), selectedAlarm, requireActivity().applicationContext
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
                selectedAlarm = music
            }
        }
    }

    private fun stringToWords(s: String) = s.trim().splitToSequence(',')
        .filter { it.isNotEmpty() } // or: .filter { it.isNotBlank() }
        .toList()

    override fun onCheckedChanged(view: CompoundButton?, p1: Boolean) {
        if (view is CheckBox) {
            val checked: Boolean = view.isChecked

            when (view.id) {
                R.id.monCheckBox -> {
                    if (checked) {
                        selectedDays.add(1)
                    } else {
                        selectedDays.remove(1)
                    }
                }
                R.id.tueCheckBox -> {
                    if (checked) {
                        selectedDays.add(2)
                    } else {
                        selectedDays.remove(2)
                    }
                }
                R.id.wedCheckBox -> {
                    if (checked) {
                        selectedDays.add(3)
                    } else {
                        selectedDays.remove(3)
                    }
                }
                R.id.thuCheckBox -> {
                    if (checked) {
                        selectedDays.add(4)
                    } else {
                        selectedDays.remove(4)
                    }
                }
                R.id.friCheckBox -> {
                    if (checked) {
                        selectedDays.add(5)
                    } else {
                        selectedDays.remove(5)
                    }
                }
                R.id.satCheckBox -> {
                    if (checked) {
                        selectedDays.add(6)
                    } else {
                        selectedDays.remove(6)
                    }
                }
                R.id.sunCheckBox -> {
                    if (checked) {
                        selectedDays.add(7)
                    } else {
                        selectedDays.remove(7)
                    }
                }
            }
        }
    }


}
