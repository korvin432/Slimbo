package com.mindyapps.slimbo.ui.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.ui.adapters.RecordingsRecyclerAdapter
import kotlinx.coroutines.launch

class HistoryFragment : Fragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: HistoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var recordingsAdapter: RecordingsRecyclerAdapter
    private lateinit var observerRecordings: Observer<List<Recording>>

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (root == null) {
            root = inflater.inflate(R.layout.fragment_history, container, false)
            recyclerView = root!!.findViewById(R.id.recordings_recycler)

            viewModel = ViewModelProvider(
                this, HistoryViewModelFactory(repository, requireActivity().application)
            ).get(HistoryViewModel::class.java)
            bindRecyclerView()
        }
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setSubscriber()
    }

    private fun setSubscriber() {
        observerRecordings = Observer { recordings ->
            if (recordings.isNotEmpty()) {
                recordingsAdapter.setRecordings(recordings)
            }
        }
        loaRecordings()
    }

    private fun loaRecordings() {
        lifecycleScope.launch {
            viewModel.allRecordings.observe(viewLifecycleOwner, observerRecordings)
        }
    }

    private fun bindRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recordingsAdapter =
            RecordingsRecyclerAdapter(
                ArrayList<Recording>().toMutableList(),
                requireActivity().applicationContext
            )
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = recordingsAdapter
        recordingsAdapter.onItemClick = { recording ->
            val bundle = bundleOf("recording" to recording)
            findNavController().navigate(R.id.recording_fragment, bundle)
        }
    }
}
