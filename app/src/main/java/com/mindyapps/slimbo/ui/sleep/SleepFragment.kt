package com.mindyapps.slimbo.ui.sleep

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.mindyapps.slimbo.MainActivity
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.preferences.AlarmStore
import com.mindyapps.slimbo.preferences.SleepingStore
import com.mindyapps.slimbo.ui.adapters.SelectedFactorsRecyclerAdapter
import com.mindyapps.slimbo.ui.sleeping.SleepingActivity
import io.alterac.blurkit.BlurLayout
import kotlinx.android.synthetic.main.fragment_sleep.*


class SleepFragment : Fragment(), View.OnClickListener {

    private lateinit var sleepViewModel: SleepViewModel
    private lateinit var factorsCard: CardView
    private lateinit var musicCard: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicBlurLayout: BlurLayout
    private lateinit var factorsBlurLayout: BlurLayout
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

    private var root: View? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if ((activity as MainActivity?)!!.recording != null) {
            val bundle = bundleOf(
                "recording" to ((activity as MainActivity?)!!.recording))
                        findNavController().navigate(R.id.recordingFragment, bundle)
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
            loginActivityBackground.alpha = 99

            factorsCard = root!!.findViewById(R.id.factors_card)
            factorsCard.background.alpha = 20
            musicCard = root!!.findViewById(R.id.sound_card)
            musicCard.background.alpha = 20
            musicBlurLayout = root!!.findViewById(R.id.blurLayoutMusic)
            factorsBlurLayout = root!!.findViewById(R.id.blurLayoutFactors)
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

        } else {
            musicBlurLayout.startBlur()
            factorsBlurLayout.startBlur()
        }


        return root
    }

    override fun onStart() {
        super.onStart()
        musicBlurLayout.startBlur()
        factorsBlurLayout.startBlur()
        musicBlurLayout.lockView()
        factorsBlurLayout.lockView()
    }

    override fun onResume() {
        super.onResume()
        musicBlurLayout.startBlur()
        factorsBlurLayout.startBlur()
        requireContext().theme.applyStyle(R.style.AppThemeTransparent, true)
        (requireActivity() as AppCompatActivity).supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    override fun onPause() {
        super.onPause()
        musicBlurLayout.pauseBlur()
        factorsBlurLayout.pauseBlur()
        (requireActivity() as AppCompatActivity).supportActionBar!!.setBackgroundDrawable(
            ColorDrawable(ContextCompat.getColor(requireContext(), R.color.activity_bg))
        )
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
