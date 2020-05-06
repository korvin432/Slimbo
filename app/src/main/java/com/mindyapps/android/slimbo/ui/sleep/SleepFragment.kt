package com.mindyapps.android.slimbo.ui.sleep

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Factor
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.ui.adapters.SelectedFactorsRecyclerAdapter
import io.alterac.blurkit.BlurLayout
import kotlinx.android.synthetic.main.fragment_sleep.*
import java.text.DateFormat
import java.text.DateFormat.getTimeInstance
import java.util.*
import kotlin.collections.ArrayList


class SleepFragment : Fragment(), View.OnClickListener {

    private lateinit var sleepViewModel: SleepViewModel
    private lateinit var factorsCard: CardView
    private lateinit var musicCard: CardView
    private lateinit var alarmCard: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var musicBlurLayout: BlurLayout
    private lateinit var factorsBlurLayout: BlurLayout


    private lateinit var selectedFactorsRecyclerAdapter: SelectedFactorsRecyclerAdapter
    private var selectedFactors: ArrayList<Factor>? = ArrayList()
    private var selectedMusic: Music? = null
    private var selectedLength: String? = null
    private var selectedAlarm: Music? = null
    private var firstAlarm: IntArray? = null
    private var secondAlarm: IntArray? = null
    private var thirdAlarm: IntArray? = null


    private var root: View? = null

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
            loginActivityBackground.alpha = 35

            factorsCard = root!!.findViewById(R.id.factors_card)
            musicCard = root!!.findViewById(R.id.sound_card)
            alarmCard = root!!.findViewById(R.id.alarm_card)
            musicBlurLayout = root!!.findViewById(R.id.blurLayoutMusic)
            factorsBlurLayout = root!!.findViewById(R.id.blurLayoutFactors)
            recyclerView = root!!.findViewById(R.id.selected_factors_recycler)


            factorsCard.setOnClickListener(this)
            musicCard.setOnClickListener(this)
            alarmCard.setOnClickListener(this)

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
    }

    override fun onResume() {
        super.onResume()
        musicBlurLayout.startBlur()
        factorsBlurLayout.startBlur()
    }

    override fun onPause() {
        super.onPause()
        musicBlurLayout.pauseBlur()
        factorsBlurLayout.pauseBlur()
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

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Music>(
            "selected_alarm_sound"
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                selectedAlarm = result

                var summaryTimeText = getAlarmTime(firstAlarm)

                if (secondAlarm != null){
                    summaryTimeText += "   " + getAlarmTime(secondAlarm)
                }
                if (thirdAlarm != null){
                    summaryTimeText += "   " + getAlarmTime(thirdAlarm)
                }

                selected_alarm_textview.text = summaryTimeText
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<IntArray>(
            "first_alarm"
        )?.observe(viewLifecycleOwner) { result ->
            if (result != null) {
                firstAlarm = result
            }
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<IntArray>(
            "second_alarm"
        )?.observe(viewLifecycleOwner) { result ->
                secondAlarm = result
        }

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<IntArray>(
            "third_alarm"
        )?.observe(viewLifecycleOwner) { result ->
                thirdAlarm = result
        }
    }

    private fun getAlarmTime(array: IntArray?):String{
        val cal = Calendar.getInstance()
        if (!android.text.format.DateFormat.is24HourFormat(requireContext())) {
            cal.set(Calendar.HOUR, array!![0])
        } else {
            cal.set(Calendar.HOUR_OF_DAY, array!![0])
        }
        cal.set(Calendar.MINUTE, array[1])
        return getTimeInstance(DateFormat.SHORT).format(cal.time)
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
            R.id.alarm_card -> {
                if (selectedAlarm != null && secondAlarm != null && thirdAlarm != null) {
                    val bundle = bundleOf(
                        "selected_alarm_sound" to selectedAlarm,
                        "first_alarm" to firstAlarm,
                        "second_alarm" to secondAlarm,
                        "third_alarm" to thirdAlarm
                    )
                    findNavController().navigate(R.id.select_alarm_dialog, bundle)
                } else if(selectedAlarm != null && secondAlarm != null) {
                    val bundle = bundleOf(
                        "selected_alarm_sound" to selectedAlarm,
                        "first_alarm" to firstAlarm,
                        "second_alarm" to secondAlarm
                    )
                    findNavController().navigate(R.id.select_alarm_dialog, bundle)
                } else if(selectedAlarm != null && firstAlarm != null) {
                    val bundle = bundleOf(
                        "selected_alarm_sound" to selectedAlarm,
                        "first_alarm" to firstAlarm
                    )
                    findNavController().navigate(R.id.select_alarm_dialog, bundle)
                }  else {
                    findNavController().navigate(R.id.select_alarm_dialog)
                }
            }
        }
    }
}
