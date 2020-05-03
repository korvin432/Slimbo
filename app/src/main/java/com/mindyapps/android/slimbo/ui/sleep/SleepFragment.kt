package com.mindyapps.android.slimbo.ui.sleep

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Factor


class SleepFragment : Fragment(), View.OnClickListener {

    private lateinit var sleepViewModel: SleepViewModel
    private lateinit var factorsCard: CardView


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
            factorsCard.setOnClickListener(this)
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.
            getLiveData<ArrayList<Factor>>("factors")?.observe(
            viewLifecycleOwner
        ) { result ->
            Log.d("qwwe", result.toString())
        }
    }

    override fun onClick(v: View?) {
        when (v!!.id) {
            R.id.factors_card -> {
                findNavController().navigate(R.id.factors_dialog)
            }
        }
    }
}
