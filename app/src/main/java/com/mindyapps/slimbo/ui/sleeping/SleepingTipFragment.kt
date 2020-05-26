package com.mindyapps.slimbo.ui.sleeping

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.preference.PreferenceManager

import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.preferences.SleepingStore
import kotlinx.android.synthetic.main.fragment_sleeping_tip.*


class SleepingTipFragment : Fragment() {

    private var selectedMusic: Music? = null
    private var selectedLength: String? = null
    private var selectedFactors: ArrayList<Factor>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (requireArguments().getParcelable<Music>("selected_music") != null) {
            selectedMusic = requireArguments().getParcelable("selected_music")
            selectedLength = requireArguments().getString("selected_length")
        }
        selectedFactors = requireArguments().getParcelableArrayList("selected_factors")

        return inflater.inflate(R.layout.fragment_sleeping_tip, container, false)
    }

    private fun startSleeping(){
        val bundle = bundleOf(
            "music" to selectedMusic,
            "duration" to selectedLength,
            "factors" to selectedFactors
        )
        findNavController().navigate(R.id.sleepingActivity, bundle)
        requireActivity().onBackPressed()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        button_dont_show.setOnClickListener {
            SleepingStore(PreferenceManager.getDefaultSharedPreferences(requireContext())).showTip =
                false
            startSleeping()
        }
        button_continue_sleeping.setOnClickListener {
            startSleeping()
        }
    }

}