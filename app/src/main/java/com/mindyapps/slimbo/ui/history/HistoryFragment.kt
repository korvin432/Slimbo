package com.mindyapps.slimbo.ui.history

import android.R.color
import android.app.DatePickerDialog
import android.graphics.BlendMode
import android.graphics.BlendModeColorFilter
import android.graphics.ColorFilter
import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.getColor
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.ui.adapters.RecordingsRecyclerAdapter
import kotlinx.coroutines.launch
import java.util.*
import kotlin.collections.ArrayList


class HistoryFragment : Fragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: HistoryViewModel
    private lateinit var menu: Menu
    private lateinit var recyclerView: RecyclerView
    private lateinit var recordingsAdapter: RecordingsRecyclerAdapter
    private lateinit var observerRecordings: Observer<List<Recording>>

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        setHasOptionsMenu(true)

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

    override fun onPrepareOptionsMenu(menu: Menu) {
        super.onPrepareOptionsMenu(menu)

        val itemSearch = menu.findItem(R.id.search)
        val itemRefresh = menu.findItem(R.id.refresh)
        val iconSearch = ContextCompat.getDrawable(requireContext(), R.drawable.ic_calendar)
        val iconRefresh = ContextCompat.getDrawable(requireContext(), R.drawable.ic_refresh)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            iconSearch?.colorFilter =
                BlendModeColorFilter(R.color.colorWhiteText, BlendMode.SRC_ATOP)
            iconRefresh?.colorFilter =
                BlendModeColorFilter(R.color.colorWhiteText, BlendMode.SRC_ATOP)
        } else {
            iconSearch?.setColorFilter(
                getColor(requireContext(), R.color.colorWhiteText),
                PorterDuff.Mode.SRC_ATOP
            )
            iconRefresh?.setColorFilter(
                getColor(requireContext(), R.color.colorWhiteText),
                PorterDuff.Mode.SRC_ATOP
            )
        }
        itemSearch.icon = iconSearch
        itemRefresh.icon = iconRefresh
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        this.menu = menu
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.history_menu, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.search) {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)

            val dpd = DatePickerDialog(
                requireActivity(),
                DatePickerDialog.OnDateSetListener { view, selectedYear, monthOfYear, dayOfMonth ->
                    val from = Calendar.getInstance()
                    from.set(selectedYear, monthOfYear, dayOfMonth)
                    from.set(Calendar.HOUR_OF_DAY, 0)
                    from.set(Calendar.MINUTE, 0)
                    from.set(Calendar.SECOND, 0)

                    val to = Calendar.getInstance()
                    to.set(selectedYear, monthOfYear, dayOfMonth)
                    to.set(Calendar.HOUR_OF_DAY, 24)
                    to.set(Calendar.MINUTE, 0)
                    to.set(Calendar.SECOND, 0)

                    viewModel.searchRecordings(from.timeInMillis, to.timeInMillis)
                    menu.findItem(R.id.refresh).isVisible = true
                },
                year,
                month,
                day
            )

            dpd.show()
            return true
        } else if (item.itemId == R.id.refresh) {
            viewModel.searchAll()
            menu.findItem(R.id.refresh).isVisible = false
            return true
        }
        return NavigationUI.onNavDestinationSelected(item, requireView().findNavController())
                || super.onOptionsItemSelected(item)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        setSubscriber()
    }

    private fun setSubscriber() {
        observerRecordings = Observer { recordings ->
            Log.d("qwwe", "got $recordings")
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
