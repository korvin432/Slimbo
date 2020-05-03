package com.mindyapps.android.slimbo.ui.factors

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Factor
import com.mindyapps.android.slimbo.data.repository.SlimboRepository
import com.mindyapps.android.slimbo.data.repository.SlimboRepositoryImpl
import kotlinx.coroutines.launch


class FactorsFragment : DialogFragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: FactorsViewModel
    private lateinit var recyclerView: RecyclerView

    private lateinit var observerFactors: Observer<List<Factor>>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val root = inflater.inflate(R.layout.factors_fragment, container, false)
        this.dialog!!.requestWindowFeature(Window.FEATURE_NO_TITLE)

        recyclerView = root.findViewById(R.id.factors_recycler)
        viewModel =
            ViewModelProvider(
                this,
                FactorsViewModelFactory(repository, requireActivity().application)
            ).get(FactorsViewModel::class.java)
        bindRecyclerView()
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        observerFactors = Observer { newsSource ->
            if (newsSource.isNotEmpty()) {
                //newsRecyclerAdapter.setArticles(newsSource)
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

    }

}
