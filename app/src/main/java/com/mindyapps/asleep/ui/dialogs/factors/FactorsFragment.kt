package com.mindyapps.asleep.ui.dialogs.factors

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.VERTICAL
import com.google.android.material.button.MaterialButton
import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Factor
import com.mindyapps.asleep.data.repository.SlimboRepositoryImpl
import com.mindyapps.asleep.ui.adapters.FactorsRecyclerAdapter
import kotlinx.coroutines.launch
import kotlin.collections.ArrayList


class FactorsFragment : DialogFragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: FactorsViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var confirmButton: MaterialButton
    private lateinit var factorsRecyclerAdapter: FactorsRecyclerAdapter

    private val sourceList = ArrayList<Factor>()
    private var selectedFactors = ArrayList<Factor>()
    private lateinit var observerFactors: Observer<List<Factor>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.factors_fragment, container, false)
        this.dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        this.dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)

        if (requireArguments().getParcelableArray("selected_factors") != null) {
            val categories  =
                requireArguments().getParcelableArray("selected_factors") as Array<Factor>
            if (categories.size > 1) {
                selectedFactors = categories.toList() as ArrayList<Factor>
            } else {
                selectedFactors.add(categories[0])
            }
        }
        confirmButton = root.findViewById(R.id.confirm_button)
        recyclerView = root.findViewById(R.id.factors_recycler)
        viewModel =
            ViewModelProvider(
                this,
                FactorsViewModelFactory(repository, requireActivity().application)
            ).get(FactorsViewModel::class.java)
        confirmButton.setOnClickListener { confirmFactors() }
        bindRecyclerView()
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observerFactors = Observer { newsSource ->
            if (newsSource.isNotEmpty()) {
                factorsRecyclerAdapter.setFactors(newsSource)
            }
        }
        loadFactors()
    }

    private fun loadFactors() {
        lifecycleScope.launch {
            viewModel.allFactors.observe(viewLifecycleOwner, observerFactors)
        }
    }

    private fun bindRecyclerView() {
        factorsRecyclerAdapter =
            FactorsRecyclerAdapter(
                sourceList.toMutableList(),
                selectedFactors.toMutableList(),
                requireActivity().applicationContext
            )
        recyclerView.layoutManager =
            GridLayoutManager(requireActivity().applicationContext, 3, VERTICAL, false)
        recyclerView.adapter = factorsRecyclerAdapter
        factorsRecyclerAdapter.onItemClick = { factor, selected ->
            if (selected) {
                selectedFactors.add(factor)
            } else {
                selectedFactors.remove(factor)
            }
        }
    }

    private fun confirmFactors() {
        lifecycleScope.launch {
            findNavController().previousBackStackEntry?.savedStateHandle?.set(
                "factors",
                selectedFactors
            )
        }
        dismiss()
    }


}
