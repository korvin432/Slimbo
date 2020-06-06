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
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.model.GradientColor
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.CustomBarChartRender
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList


class StatisticsFragment : Fragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var observerRecordings: Observer<List<Recording>>
    private lateinit var allRecordings: List<Recording>
    private lateinit var goodRecordings: List<Recording>
    private lateinit var badRecordings: List<Recording>
    private lateinit var frequencyChart: BarChart
    private lateinit var durationChart: LineChart

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (root == null) {
            root = inflater.inflate(R.layout.statistics_fragment, container, false)
            frequencyChart = root!!.findViewById(R.id.frequency_chart)
            durationChart = root!!.findViewById(R.id.duration_chart)
        }
        return root
    }

    private fun initCharts() {
        setFrequencyChart()
        setDurationChart()
    }

    private fun setFrequencyChart() {
        frequencyChart.setDrawBarShadow(false)
        frequencyChart.description.isEnabled = false
        frequencyChart.legend.isEnabled = false
        frequencyChart.setPinchZoom(false)
        frequencyChart.setTouchEnabled(false)
        frequencyChart.setMaxVisibleValueCount(60)

        val barChartRender = CustomBarChartRender(
            frequencyChart,
            frequencyChart.animator,
            frequencyChart.viewPortHandler
        )
        barChartRender.setContext(requireContext())
        barChartRender.setRadius(40)
        frequencyChart.renderer = barChartRender


        val xAxis = frequencyChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        frequencyChart.axisLeft.setDrawGridLines(true)
        frequencyChart.axisLeft.gridColor = R.color.colorLightHint
        frequencyChart.axisLeft.setDrawAxisLine(false)
        frequencyChart.axisRight.isEnabled = false
        frequencyChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        frequencyChart.axisLeft.textSize = 15f
        frequencyChart.axisLeft.granularity = 1.0f
        frequencyChart.axisLeft.isGranularityEnabled = true


        val daysOfTheWeek = resources.getStringArray(R.array.days_of_week)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() > -1 && value.toInt() < 7) {
                    daysOfTheWeek[value.toInt()]
                } else ""
            }
        }

        frequencyChart.data = setFrequencyData(getSnoreCountList())
        frequencyChart.animateY(1000)

    }

    private fun setDurationChart() {
        durationChart.description.isEnabled = false
        durationChart.setDrawGridBackground(false)
        durationChart.setDrawMarkers(false)
        durationChart.setPinchZoom(false)
        durationChart.setTouchEnabled(false)
        durationChart.setScaleEnabled(false)
        durationChart.legend.isEnabled = false

        val xAxis = durationChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.isGranularityEnabled = true
        xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        durationChart.axisLeft.setDrawGridLines(true)
        durationChart.axisLeft.gridColor = R.color.colorLightHint
        durationChart.axisLeft.setDrawAxisLine(false)
        durationChart.axisRight.isEnabled = false
        durationChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        durationChart.axisLeft.textSize = 15f
        durationChart.axisLeft.granularity = 1.0f
        durationChart.axisLeft.isGranularityEnabled = true
        durationChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return try {
                    var timeInSeconds: Int = (value.toLong() / 1000).toInt()
                    val hours: Int
                    val minutes: Int
                    hours = timeInSeconds / 3600
                    timeInSeconds -= hours * 3600
                    minutes = timeInSeconds / 60
                    String.format("%02d", hours) + ":" + String.format("%02d", minutes)
                } catch (e: Exception) {
                    value.toString()
                }
            }

        }

        val daysOfTheWeek = resources.getStringArray(R.array.days_of_week)
        xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() > -1 && value.toInt() < 7) {
                    daysOfTheWeek[value.toInt()]
                } else ""
            }
        }
        durationChart.data = setDurationData(getSleepDurationList())
        durationChart.animateY(1000)
    }

    private fun getSnoreCountList(): Map<String, Int> {
        val snoreMap = mutableMapOf<String, Int>()
        allRecordings.forEach { rec ->
            val sdf = SimpleDateFormat("EEE")
            val dayString: String = sdf.format(Date(rec.sleep_at_time!!))
            if (rec.recordings != null && rec.recordings.size > 0) {
                snoreMap[dayString] = rec.recordings.size
            } else {
                snoreMap[dayString] = 0
            }
        }
        val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
        daysOfTheWeek.forEach {
            if (!snoreMap.keys.contains(it) && it != "") {
                snoreMap[it] = 0
            }
        }
        return snoreMap
    }

    private fun getSleepDurationList(): Map<String, Long> {
        val snoreMap = mutableMapOf<String, Long>()
        allRecordings.forEach { rec ->
            val sdf = SimpleDateFormat("EEE")
            val dayString: String = sdf.format(Date(rec.sleep_at_time!!))
            snoreMap[dayString] = rec.wake_up_time!! - rec.sleep_at_time
        }

        val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
        daysOfTheWeek.forEach {
            if (!snoreMap.keys.contains(it) && it != "") {
                snoreMap[it] = 0
            }
        }
        Log.d("qwwe", "map ${snoreMap}")
        return snoreMap
    }

    private fun setDurationData(map: Map<String, Long>): LineData {
        val entries: ArrayList<Entry> = ArrayList()
        val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
        entries.add(Entry(0f, (map[daysOfTheWeek[2]])!!.toFloat()))
        entries.add(Entry(1f, (map[daysOfTheWeek[3]])!!.toFloat()))
        entries.add(Entry(2f, (map[daysOfTheWeek[4]])!!.toFloat()))
        entries.add(Entry(3f, (map[daysOfTheWeek[5]])!!.toFloat()))
        entries.add(Entry(4f, (map[daysOfTheWeek[6]])!!.toFloat()))
        entries.add(Entry(5f, (map[daysOfTheWeek[7]])!!.toFloat()))
        entries.add(Entry(6f, (map[daysOfTheWeek[1]])!!.toFloat()))

        val lineDataSet = LineDataSet(entries, "")
        lineDataSet.setDrawValues(false)
        lineDataSet.setDrawCircles(false)
        val startColor = ContextCompat.getColor(requireContext(), R.color.colorAccent)
        val endColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)

        val gradientColors: MutableList<GradientColor> = ArrayList()
        gradientColors.add(GradientColor(endColor, startColor))
        lineDataSet.gradientColors = gradientColors
        lineDataSet.fillAlpha = 255
        lineDataSet.setDrawFilled(true)
        lineDataSet.fillDrawable =
            ContextCompat.getDrawable(requireContext(), R.drawable.chart_gradient)
        return LineData(lineDataSet, lineDataSet)
    }

    private fun setFrequencyData(map: Map<String, Int>): BarData {
        val entries: ArrayList<BarEntry> = ArrayList()
        val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
        entries.add(BarEntry(0f, (map[daysOfTheWeek[2]])!!.toFloat()))
        entries.add(BarEntry(1f, (map[daysOfTheWeek[3]])!!.toFloat()))
        entries.add(BarEntry(2f, (map[daysOfTheWeek[4]])!!.toFloat()))
        entries.add(BarEntry(3f, (map[daysOfTheWeek[5]])!!.toFloat()))
        entries.add(BarEntry(4f, (map[daysOfTheWeek[6]])!!.toFloat()))
        entries.add(BarEntry(5f, (map[daysOfTheWeek[7]])!!.toFloat()))
        entries.add(BarEntry(6f, (map[daysOfTheWeek[1]])!!.toFloat()))


        val barDataSet = BarDataSet(entries, "")
        barDataSet.setDrawValues(false)
        val startColor = ContextCompat.getColor(requireContext(), R.color.colorAccent)
        val endColor = ContextCompat.getColor(requireContext(), R.color.colorPrimary)

        val gradientColors: MutableList<GradientColor> = ArrayList()
        gradientColors.add(GradientColor(endColor, startColor))
        barDataSet.gradientColors = gradientColors
        return BarData(barDataSet, barDataSet)
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
