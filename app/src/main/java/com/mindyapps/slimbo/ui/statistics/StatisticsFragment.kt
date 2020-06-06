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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis.XAxisPosition
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.model.GradientColor
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Factor
import com.mindyapps.slimbo.data.model.Recording
import com.mindyapps.slimbo.data.repository.SlimboRepositoryImpl
import com.mindyapps.slimbo.internal.CustomBarChartRender
import com.mindyapps.slimbo.internal.Sorter
import com.mindyapps.slimbo.ui.adapters.FactorProgressAdapter
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class StatisticsFragment : Fragment() {

    private var repository = SlimboRepositoryImpl()
    private lateinit var viewModel: StatisticsViewModel
    private lateinit var observerRecordings: Observer<List<Recording>>
    private lateinit var allRecordings: List<Recording>
    private lateinit var frequencyChart: BarChart
    private lateinit var sleepDurationChart: LineChart
    private lateinit var snoreDurationChart: LineChart
    private lateinit var goodRecyclerView: RecyclerView
    private lateinit var badRecyclerView: RecyclerView
    private lateinit var progressAdapter: FactorProgressAdapter

    private var root: View? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (root == null) {
            root = inflater.inflate(R.layout.statistics_fragment, container, false)
            frequencyChart = root!!.findViewById(R.id.frequency_chart)
            sleepDurationChart = root!!.findViewById(R.id.duration_chart)
            snoreDurationChart = root!!.findViewById(R.id.snore_duration_chart)
            goodRecyclerView = root!!.findViewById(R.id.good_recycler)
            badRecyclerView = root!!.findViewById(R.id.bad_recycler)
        }
        return root
    }

    private fun initCharts() {
        setFrequencyChart()
        setDurationChart()
        setSnoreDurationChart()
        setGoodRecyclerView()
        setBadRecyclerView()
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

    private fun setDurationChart() {
        sleepDurationChart.description.isEnabled = false
        sleepDurationChart.setDrawGridBackground(false)
        sleepDurationChart.setDrawMarkers(false)
        sleepDurationChart.setPinchZoom(false)
        sleepDurationChart.setTouchEnabled(false)
        sleepDurationChart.setScaleEnabled(false)
        sleepDurationChart.legend.isEnabled = false

        val xAxis = sleepDurationChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.isGranularityEnabled = true
        xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        sleepDurationChart.axisLeft.setDrawGridLines(true)
        sleepDurationChart.axisLeft.gridColor = R.color.colorLightHint
        sleepDurationChart.axisLeft.setDrawAxisLine(false)
        sleepDurationChart.axisRight.isEnabled = false
        sleepDurationChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        sleepDurationChart.axisLeft.textSize = 15f
        sleepDurationChart.axisLeft.granularity = 1.0f
        sleepDurationChart.axisLeft.isGranularityEnabled = true
        sleepDurationChart.axisLeft.valueFormatter = object : ValueFormatter() {
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
        sleepDurationChart.data = setDurationData(getSleepDurationList())
        sleepDurationChart.animateY(1000)
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

    private fun setSnoreDurationChart() {
        snoreDurationChart.description.isEnabled = false
        snoreDurationChart.setDrawGridBackground(false)
        snoreDurationChart.setDrawMarkers(false)
        snoreDurationChart.setPinchZoom(false)
        snoreDurationChart.setTouchEnabled(false)
        snoreDurationChart.setScaleEnabled(false)
        snoreDurationChart.legend.isEnabled = false

        val xAxis = snoreDurationChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.isGranularityEnabled = true
        xAxis.labelCount = 7
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        snoreDurationChart.axisLeft.setDrawGridLines(true)
        snoreDurationChart.axisLeft.gridColor = R.color.colorLightHint
        snoreDurationChart.axisLeft.setDrawAxisLine(false)
        snoreDurationChart.axisRight.isEnabled = false
        snoreDurationChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        snoreDurationChart.axisLeft.textSize = 15f
        snoreDurationChart.axisLeft.granularity = 1.0f
        snoreDurationChart.axisLeft.isGranularityEnabled = true
        snoreDurationChart.axisLeft.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return try {
                    String.format(
                        "%02d:%02d",
                        TimeUnit.MILLISECONDS.toMinutes(value.toLong()),
                        TimeUnit.MILLISECONDS.toSeconds(value.toLong()) -
                                TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(value.toLong()))
                    )
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
        snoreDurationChart.data = setDurationData(getSnoreDurationList())
        snoreDurationChart.animateY(1000)
    }

    private fun getSnoreDurationList(): Map<String, Long> {
        val snoreMap = mutableMapOf<String, Long>()
        allRecordings.forEach { rec ->
            var duration: Long = 0

            rec.recordings!!.forEach {
                Log.d("qwwe", "duration ${it.duration}")
                duration += it.duration!!
            }
            val sdf = SimpleDateFormat("EEE")
            val dayString: String = sdf.format(Date(rec.sleep_at_time!!))
            snoreMap[dayString] = duration
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

    private fun setGoodRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        goodRecyclerView.setHasFixedSize(true)

        val goodFactors: MutableList<Factor> = LinkedList()
        val goodMap = mutableMapOf<Factor, Int>()
        val finalMap = mutableMapOf<Factor, Int>()
        var goodCount = 0

        allRecordings.forEach { rec ->
            if (rec.rating in 4..5) {
                goodCount++
                if (rec.factors != null && rec.factors.size > 0) {
                    goodFactors.addAll(rec.factors)
                }
            }
        }

        allRecordings.forEach { rec ->
            if (rec.factors != null) {
                rec.factors.forEach { fac ->
                    val count = Collections.frequency(goodFactors, fac)
                    if (count > 0) {
                        goodMap[fac] = count
                    }
                }
            }
        }

        for ((key, value) in Sorter().entriesSortedByValues(goodMap)) {
            if (finalMap.size < 3) {
                finalMap[key] = value
            }
        }

        progressAdapter = FactorProgressAdapter(
            finalMap as LinkedHashMap<Factor, Int>,
            goodCount,
            true,
            requireActivity().applicationContext
        )
        goodRecyclerView.layoutManager = linearLayoutManager
        goodRecyclerView.adapter = progressAdapter
    }

    private fun setBadRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        badRecyclerView.setHasFixedSize(true)

        val badFactors: MutableList<Factor> = LinkedList()
        val badMap = mutableMapOf<Factor, Int>()
        val finalMap = mutableMapOf<Factor, Int>()
        var badCount = 0

        allRecordings.forEach { rec ->
            if (rec.rating in 1..2) {
                badCount++
                if (rec.factors != null && rec.factors.size > 0) {
                    badFactors.addAll(rec.factors)
                }
            }
        }

        allRecordings.forEach { rec ->
            if (rec.factors != null) {
                rec.factors.forEach { fac ->
                    val count = Collections.frequency(badFactors, fac)
                    if (count > 0) {
                        badMap[fac] = count
                    }
                }
            }
        }

        for ((key, value) in Sorter().entriesSortedByValues(badMap)) {
            if (finalMap.size < 3) {
                finalMap[key] = value
            }
        }

        progressAdapter = FactorProgressAdapter(
            finalMap as LinkedHashMap<Factor, Int>,
            badCount,
            false,
            requireActivity().applicationContext
        )
        badRecyclerView.layoutManager = linearLayoutManager
        badRecyclerView.adapter = progressAdapter
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
