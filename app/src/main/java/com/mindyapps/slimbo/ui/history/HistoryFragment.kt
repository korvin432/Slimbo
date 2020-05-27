package com.mindyapps.slimbo.ui.history

import android.media.MediaPlayer
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Music
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.ui.adapters.RecordingsRecyclerAdapter
import com.mindyapps.slimbo.ui.adapters.SelectedMusicAdapter
import kotlinx.coroutines.launch
import java.io.File

class HistoryFragment : Fragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: HistoryViewModel
    private lateinit var recyclerView: RecyclerView
    private lateinit var recordingsAdapter: RecordingsRecyclerAdapter
    private lateinit var observerRecordings: Observer<List<Recording>>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = ViewModelProvider(
            this,
            HistoryViewModelFactory(repository, requireActivity().application)
        ).get(HistoryViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_history, container, false)

        recyclerView = root.findViewById(R.id.recordings_recycler)
        return root
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setSubscriber()
    }

    private fun setSubscriber() {
        observerRecordings = Observer { recordings ->
            if (recordings.isNotEmpty()) {
                bindRecyclerView(recordings)
            }
        }
        loaRecordings()
    }

    private fun loaRecordings() {
        lifecycleScope.launch {
            viewModel.allRecordings.observe(viewLifecycleOwner, observerRecordings)
        }
    }

    private fun bindRecyclerView(recordings: List<Recording>) {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        recordingsAdapter =
            RecordingsRecyclerAdapter(recordings, requireActivity().applicationContext)
        recyclerView.layoutManager = linearLayoutManager
        recyclerView.adapter = recordingsAdapter
        recordingsAdapter.onItemClick = { recording ->
            val bundle = bundleOf("recording" to recording)
            findNavController().navigate(R.id.recording_fragment, bundle)
        }
    }
}
