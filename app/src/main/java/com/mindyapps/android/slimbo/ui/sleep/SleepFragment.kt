package com.mindyapps.android.slimbo.ui.sleep

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.Space
import androidx.cardview.widget.CardView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.observe
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Factor
import com.mindyapps.android.slimbo.data.model.Music
import com.mindyapps.android.slimbo.ui.adapters.FactorsRecyclerAdapter
import com.mindyapps.android.slimbo.ui.adapters.SelectedFactorsRecyclerAdapter
import kotlinx.android.synthetic.main.fragment_sleep.*


class SleepFragment : Fragment(), View.OnClickListener {

    private lateinit var sleepViewModel: SleepViewModel
    private lateinit var factorsCard: CardView
    private lateinit var musicCard: CardView
    private lateinit var recyclerView: RecyclerView
    private lateinit var factorsSpace: View
    private lateinit var selectedFactorsRecyclerAdapter: SelectedFactorsRecyclerAdapter
    private var selectedFactors: ArrayList<Factor>? = ArrayList()
    private var selectedMusic: Music? = null


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
            factorsSpace = root!!.findViewById(R.id.factors_space)
            musicCard = root!!.findViewById(R.id.sound_card)
            recyclerView = root!!.findViewById(R.id.selected_factors_recycler)


            factorsCard.setOnClickListener(this)
            musicCard.setOnClickListener(this)
        }


        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<ArrayList<Factor>>(
            "factors"
        )?.observe(
            viewLifecycleOwner
        ) { result ->
            selectedFactors = result
            if (result.size > 0){
                factorsSpace.visibility = View.VISIBLE
            } else {
                factorsSpace.visibility = View.GONE
            }
            bindFactorsRecycler(result)
        }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<Music>(
            "selected_music"
        )?.observe(
            viewLifecycleOwner
        ) { result ->
            selectedMusic = result
            selected_music_textview.text = selectedMusic!!.name
        }
    }

    private fun bindFactorsRecycler(factors: ArrayList<Factor>) {
        selectedFactorsRecyclerAdapter =
            SelectedFactorsRecyclerAdapter(
                factors,
                requireActivity().applicationContext
            )
        recyclerView.layoutManager =
            GridLayoutManager(requireActivity().applicationContext, 3, RecyclerView.VERTICAL, false)
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
            R.id.sound_card ->{
                if (selectedMusic != null){
                    val bundle = bundleOf("selected_music" to selectedMusic)
                    findNavController().navigate(R.id.select_music_dialog, bundle)
                } else {
                    findNavController().navigate(R.id.select_music_dialog)
                }
            }
        }
    }
}
