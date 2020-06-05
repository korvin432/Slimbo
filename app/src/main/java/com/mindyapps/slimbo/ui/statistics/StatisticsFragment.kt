package com.mindyapps.slimbo.ui.statistics

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.model.GradientColor
import com.github.mikephil.charting.utils.ColorTemplate
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.CustomBarChartRender
import kotlinx.coroutines.launch


class StatisticsFragment : Fragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var observerRecordings: Observer<List<Recording>>
    private lateinit var allRecordings: List<Recording>
    private lateinit var goodRecordings: List<Recording>
    private lateinit var badRecordings: List<Recording>
    private lateinit var chart: BarChart

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (root == null) {
            root = inflater.inflate(R.layout.statistics_fragment, container, false)
            chart = root!!.findViewById(R.id.frequency_chart)
        }
        return root
    }

    private fun initCharts() {
        Log.d("qwwe", "$allRecordings")

        chart.setDrawBarShadow(false)
        chart.description.isEnabled = false
        chart.legend.isEnabled = false
        chart.setPinchZoom(false)
        chart.setTouchEnabled(false)
        chart.setMaxVisibleValueCount(60)

        val barChartRender = CustomBarChartRender(chart, chart.animator, chart.viewPortHandler)
        barChartRender.setContext(requireContext())
        barChartRender.setRadius(20)
        chart.renderer = barChartRender


        val xAxis = chart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f


        chart.axisLeft.setDrawGridLines(true)
        chart.axisLeft.gridColor = R.color.colorLightHint
        chart.axisLeft.setDrawAxisLine(false)
        chart.axisRight.isEnabled = false
        chart.axisLeft.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        chart.axisLeft.textSize = 15f

        val daysOfTheWeek = resources.getStringArray(R.array.days_of_week)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() > -1 && value.toInt() < 7) {
                    daysOfTheWeek[value.toInt()]
                } else ""
            }
        }

        setData()
    }

    private fun setData() {
        val entries: ArrayList<BarEntry> = ArrayList()
        entries.add(BarEntry( 0f,38f))
        entries.add(BarEntry(1f, 52f))
        entries.add(BarEntry(2f, 65f))
        entries.add(BarEntry(3f, 30f))
        entries.add(BarEntry(4f, 85f))
        entries.add(BarEntry(5f, 15f))
        entries.add(BarEntry(6f, 76f))

        val barDataSet = BarDataSet(entries, "")
        barDataSet.setDrawValues(false)
        val startColor = ContextCompat.getColor(requireContext(), R.color.colorAccent)
        val endColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)

        val gradientColors: MutableList<GradientColor> = ArrayList()
        gradientColors.add(GradientColor(endColor, startColor))

        barDataSet.gradientColors = gradientColors

        val data = BarData(barDataSet, barDataSet)
        chart.data = data // set the data and list of lables into chart


        chart.animateY(1000)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(
            this,
            StatisticsViewModelFactory(repository, requireActivity().application)
        ).get(StatisticsViewModel::class.java)

        setSubscriber()
    }

    private fun setSubscriber() {
        observerRecordings = Observer { recordings ->
            if (recordings.isNotEmpty()) {
                allRecordings = recordings
                initCharts()
            }
        }
        loaRecordings()
    }

    private fun loaRecordings() {
        lifecycleScope.launch {
            viewModel.recordings.observe(viewLifecycleOwner, observerRecordings)
        }
    }
}
