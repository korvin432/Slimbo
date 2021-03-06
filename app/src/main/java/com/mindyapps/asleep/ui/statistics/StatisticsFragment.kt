package com.mindyapps.asleep.ui.statistics

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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
import com.google.android.material.chip.Chip
import com.mindyapps.asleep.MainActivity
import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Factor
import com.mindyapps.asleep.data.model.Recording
import com.mindyapps.asleep.data.repository.SlimboRepositoryImpl
import com.mindyapps.asleep.internal.CustomBarChartRender
import com.mindyapps.asleep.internal.Sorter
import com.mindyapps.asleep.ui.adapters.FactorProgressAdapter
import com.mindyapps.asleep.ui.subs.SubscribeActivity
import kotlinx.coroutines.launch
import java.text.DateFormatSymbols
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.collections.ArrayList


class StatisticsFragment : Fragment(), View.OnClickListener {

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
    private lateinit var weekChip: Chip
    private lateinit var monthChip: Chip
    private lateinit var yearChip: Chip
    private lateinit var noGoodText: TextView
    private lateinit var noBadText: TextView
    private lateinit var demoText: TextView

    private var chartDays = 7
    private var root: View? = null
    private var isDemo = true

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
            weekChip = root!!.findViewById(R.id.week_chip)
            monthChip = root!!.findViewById(R.id.month_chip)
            yearChip = root!!.findViewById(R.id.year_chip)
            noGoodText = root!!.findViewById(R.id.no_good_text)
            noBadText = root!!.findViewById(R.id.no_bad_text)
            demoText = root!!.findViewById(R.id.demo_text)

            weekChip.isChecked = true

            weekChip.setOnClickListener(this)
            monthChip.setOnClickListener(this)
            yearChip.setOnClickListener(this)
            sleepDurationChart.setNoDataText("")
            snoreDurationChart.setNoDataText("")
            frequencyChart.setNoDataText("")
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
        frequencyChart.clear()
        frequencyChart.setNoDataText("")
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
        barChartRender.setRadius(50)
        if (chartDays == 30) {
            barChartRender.setRadius(12)
        } else if (chartDays == 12) {
            barChartRender.setRadius(35)
        }
        frequencyChart.renderer = barChartRender

        val xAxis = frequencyChart.xAxis
        xAxis.position = XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)
        xAxis.labelCount = chartDays
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        frequencyChart.axisLeft.setDrawGridLines(true)
        frequencyChart.axisLeft.gridColor = R.color.colorLightHint
        frequencyChart.axisLeft.setDrawAxisLine(false)
        frequencyChart.axisRight.isEnabled = false
        frequencyChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        frequencyChart.axisLeft.textSize = 13f
        frequencyChart.axisLeft.granularity = 1.0f
        frequencyChart.axisLeft.isGranularityEnabled = true


        when (chartDays) {
            7 -> {
                xAxis.labelRotationAngle = 0f
                val daysOfTheWeek = resources.getStringArray(R.array.days_of_week)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() > -1 && value.toInt() < 7) {
                            daysOfTheWeek[value.toInt()]
                        } else ""
                    }
                }
            }
            30 -> {
                xAxis.valueFormatter = null
                xAxis.labelRotationAngle = -90f
                xAxis.textSize = 10f
            }
            12 -> {
                val months = DateFormatSymbols.getInstance().shortMonths
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() > -1 && value.toInt() < 12) {
                            months[value.toInt()]
                        } else ""
                    }
                }
            }
        }

        frequencyChart.data = setFrequencyData(getSnoreCountList())
        frequencyChart.animateY(1000)
    }

    private fun getSnoreCountList(): Map<String, Int> {
        val snoreMap = mutableMapOf<String, Int>()
        allRecordings.forEach { rec ->
            val sdf = when (chartDays) {
                7 -> SimpleDateFormat("EEE")
                30 -> SimpleDateFormat("d")
                12 -> SimpleDateFormat("MMM")
                else -> SimpleDateFormat("EEE")
            }

            val dayString: String = sdf.format(Date(rec.sleep_at_time!!))
            if (rec.recordings != null && rec.recordings.size > 0) {
                if (snoreMap[dayString] != null) {
                    snoreMap[dayString] = snoreMap.getValue(dayString) + rec.recordings.size
                } else {
                    snoreMap[dayString] = rec.recordings.size
                }
            }

        }
        when (chartDays) {
            7 -> {
                val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
                daysOfTheWeek.forEach {
                    if (!snoreMap.keys.contains(it) && it != "") {
                        snoreMap[it] = 0
                    }
                }
            }
            30 -> {
                for (i in 1..30) {
                    if (!snoreMap.keys.contains(i.toString())) {
                        snoreMap[i.toString()] = 0
                    }
                }
            }
            12 -> {
                val months = DateFormatSymbols.getInstance().shortMonths
                months.forEach {
                    if (!snoreMap.keys.contains(it) && it != "") {
                        snoreMap[it] = 0
                    }
                }
            }
        }
        return snoreMap
    }

    private fun setFrequencyData(map: Map<String, Int>): BarData {
        val entries: ArrayList<BarEntry> = ArrayList()

        if (!isDemo) {
            when (chartDays) {
                7 -> {
                    val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
                    entries.add(BarEntry(0f, (map[daysOfTheWeek[2]])!!.toFloat()))
                    entries.add(BarEntry(1f, (map[daysOfTheWeek[3]])!!.toFloat()))
                    entries.add(BarEntry(2f, (map[daysOfTheWeek[4]])!!.toFloat()))
                    entries.add(BarEntry(3f, (map[daysOfTheWeek[5]])!!.toFloat()))
                    entries.add(BarEntry(4f, (map[daysOfTheWeek[6]])!!.toFloat()))
                    entries.add(BarEntry(5f, (map[daysOfTheWeek[7]])!!.toFloat()))
                    entries.add(BarEntry(6f, (map[daysOfTheWeek[1]])!!.toFloat()))
                }
                30 -> {
                    for (i in 1..30) {
                        entries.add(BarEntry(i.toFloat(), (map[i.toString()])!!.toFloat()))
                    }
                }
                12 -> {
                    val months = DateFormatSymbols.getInstance().shortMonths
                    months.forEachIndexed { index, s ->
                        entries.add(BarEntry(index.toFloat(), (map[s.toString()])!!.toFloat()))
                    }
                }
            }
        } else {
            entries.add(BarEntry(0f, 2f))
            entries.add(BarEntry(1f, 4f))
            entries.add(BarEntry(2f, 3f))
            entries.add(BarEntry(3f, 8f))
            entries.add(BarEntry(4f, 5f))
            entries.add(BarEntry(5f, 3f))
            entries.add(BarEntry(6f, 6f))
        }


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
        xAxis.labelCount = chartDays
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        sleepDurationChart.axisLeft.setDrawGridLines(true)
        sleepDurationChart.axisLeft.gridColor = R.color.colorLightHint
        sleepDurationChart.axisLeft.setDrawAxisLine(false)
        sleepDurationChart.axisRight.isEnabled = false
        sleepDurationChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        sleepDurationChart.axisLeft.textSize = 13f
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

        when (chartDays) {
            7 -> {
                xAxis.labelRotationAngle = 0f
                val daysOfTheWeek = resources.getStringArray(R.array.days_of_week)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() > -1 && value.toInt() < 7) {
                            daysOfTheWeek[value.toInt()]
                        } else ""
                    }
                }
            }
            30 -> {
                xAxis.valueFormatter = null
                xAxis.labelRotationAngle = -90f
                xAxis.textSize = 10f
            }
            12 -> {
                val months = DateFormatSymbols.getInstance().shortMonths
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() > -1 && value.toInt() < 12) {
                            months[value.toInt()]
                        } else ""
                    }
                }
            }
        }

        sleepDurationChart.data = setDurationData(getSleepDurationList(), true)
        sleepDurationChart.animateY(1000)
    }

    private fun getSleepDurationList(): Map<String, Long> {
        val snoreMap = mutableMapOf<String, Long>()
        allRecordings.forEach { rec ->
            val sdf = when (chartDays) {
                7 -> SimpleDateFormat("EEE")
                30 -> SimpleDateFormat("d")
                12 -> SimpleDateFormat("MMM")
                else -> SimpleDateFormat("EEE")
            }


            val dayString: String = sdf.format(Date(rec.sleep_at_time!!))
            if (snoreMap[dayString] != null) {
                snoreMap[dayString] =
                    snoreMap.getValue(dayString) + (rec.wake_up_time!! - rec.sleep_at_time)
            } else {
                snoreMap[dayString] = rec.wake_up_time!! - rec.sleep_at_time
            }

        }

        when (chartDays) {
            7 -> {
                val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
                daysOfTheWeek.forEach {
                    if (!snoreMap.keys.contains(it) && it != "") {
                        snoreMap[it] = 0
                    }
                }
            }
            30 -> {
                for (i in 1..30) {
                    if (!snoreMap.keys.contains(i.toString())) {
                        snoreMap[i.toString()] = 0
                    }
                }
            }
            12 -> {
                val months = DateFormatSymbols.getInstance().shortMonths
                months.forEach {
                    if (!snoreMap.keys.contains(it) && it != "") {
                        snoreMap[it] = 0
                    }
                }
            }
        }

        return snoreMap

    }

    private fun setDurationData(map: Map<String, Long>, isSleep: Boolean): LineData {
        val entries: ArrayList<Entry> = ArrayList()
        if (!isDemo) {
            when (chartDays) {
                7 -> {
                    val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
                    entries.add(Entry(0f, (map[daysOfTheWeek[2]])!!.toFloat()))
                    entries.add(Entry(1f, (map[daysOfTheWeek[3]])!!.toFloat()))
                    entries.add(Entry(2f, (map[daysOfTheWeek[4]])!!.toFloat()))
                    entries.add(Entry(3f, (map[daysOfTheWeek[5]])!!.toFloat()))
                    entries.add(Entry(4f, (map[daysOfTheWeek[6]])!!.toFloat()))
                    entries.add(Entry(5f, (map[daysOfTheWeek[7]])!!.toFloat()))
                    entries.add(Entry(6f, (map[daysOfTheWeek[1]])!!.toFloat()))

                }
                30 -> {
                    for (i in 1..30) {
                        if (map[i.toString()]?.toFloat() != null) {
                            entries.add(BarEntry(i.toFloat(), (map[i.toString()])!!.toFloat()))
                        }
                    }
                }
                12 -> {
                    val months = DateFormatSymbols.getInstance().shortMonths
                    months.forEachIndexed { index, s ->
                        entries.add(BarEntry(index.toFloat(), (map[s.toString()])!!.toFloat()))
                    }
                }
            }
        } else {
            if (isSleep) {
                entries.add(BarEntry(0f, 21800000f))
                entries.add(BarEntry(1f, 28900000f))
                entries.add(BarEntry(2f, 23440000f))
                entries.add(BarEntry(3f, 25720000f))
                entries.add(BarEntry(4f, 22700000f))
                entries.add(BarEntry(5f, 21620000f))
                entries.add(BarEntry(6f, 21600000f))
            } else {
                entries.add(BarEntry(0f, 75000f))
                entries.add(BarEntry(1f, 60000f))
                entries.add(BarEntry(2f, 80000f))
                entries.add(BarEntry(3f, 45000f))
                entries.add(BarEntry(4f, 58000f))
                entries.add(BarEntry(5f, 64000f))
                entries.add(BarEntry(6f, 90000f))
            }
        }

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
        snoreDurationChart.setNoDataText("")
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
        xAxis.labelCount = chartDays
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        xAxis.textSize = 12f

        snoreDurationChart.axisLeft.setDrawGridLines(true)
        snoreDurationChart.axisLeft.gridColor = R.color.colorLightHint
        snoreDurationChart.axisLeft.setDrawAxisLine(false)
        snoreDurationChart.axisRight.isEnabled = false
        snoreDurationChart.axisLeft.textColor =
            ContextCompat.getColor(requireContext(), R.color.colorLightHint)
        snoreDurationChart.axisLeft.textSize = 13f
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

        when (chartDays) {
            7 -> {
                val daysOfTheWeek = resources.getStringArray(R.array.days_of_week)
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() > -1 && value.toInt() < 7) {
                            daysOfTheWeek[value.toInt()]
                        } else ""
                    }
                }
                xAxis.labelRotationAngle = 0f
            }
            30 -> {
                xAxis.valueFormatter = null
                xAxis.labelRotationAngle = -90f
                xAxis.textSize = 10f
            }

            12 -> {
                val months = DateFormatSymbols.getInstance().shortMonths
                xAxis.valueFormatter = object : ValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value.toInt() > -1 && value.toInt() < 12) {
                            months[value.toInt()]
                        } else ""
                    }
                }
            }
        }

        snoreDurationChart.data = setDurationData(getSnoreDurationList(), false)
        snoreDurationChart.animateY(1000)
    }

    private fun getSnoreDurationList(): Map<String, Long> {
        val snoreMap = mutableMapOf<String, Long>()
        allRecordings.forEach { rec ->
            var duration: Long = 0
            rec.recordings!!.forEach {
                duration += it.duration!!
            }
            val sdf = when (chartDays) {
                7 -> SimpleDateFormat("EEE")
                30 -> SimpleDateFormat("d")
                12 -> SimpleDateFormat("MMM")
                else -> SimpleDateFormat("EEE")
            }

            val dayString: String = sdf.format(Date(rec.sleep_at_time!!))
            if (snoreMap[dayString] != null) {
                snoreMap[dayString] = snoreMap.getValue(dayString) + duration
            } else {
                snoreMap[dayString] = duration
            }

            when (chartDays) {
                7 -> {
                    val daysOfTheWeek = DateFormatSymbols.getInstance().shortWeekdays
                    daysOfTheWeek.forEach {
                        if (!snoreMap.keys.contains(it) && it != "") {
                            snoreMap[it] = 0
                        }
                    }
                }
                30 -> {
                    for (i in 1..30) {
                        if (!snoreMap.keys.contains(i.toString())) {
                            snoreMap[i.toString()] = 0
                        }
                    }
                }
                12 -> {
                    val months = DateFormatSymbols.getInstance().shortMonths
                    months.forEach {
                        if (!snoreMap.keys.contains(it) && it != "") {
                            snoreMap[it] = 0
                        }
                    }
                }
            }

        }
        return snoreMap
    }

    private fun setGoodRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        goodRecyclerView.setHasFixedSize(true)

        val goodFactors: MutableList<Factor> = LinkedList()
        val goodMap = mutableMapOf<Factor, Int>()
        val finalMap = mutableMapOf<Factor, Int>()
        var goodCount = 0

        if (!isDemo) {
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
        } else {
            goodCount = 5
            finalMap[Factor(null, "workout", "ic_workout")] = 3
            finalMap[Factor(null, "shower", "ic_showe")] = 2
            finalMap[Factor(null, "wine", "ic_wine")] = 1
        }

        progressAdapter = FactorProgressAdapter(
            finalMap as LinkedHashMap<Factor, Int>,
            goodCount,
            true,
            requireActivity().applicationContext
        )
        goodRecyclerView.layoutManager = linearLayoutManager
        goodRecyclerView.adapter = progressAdapter
        if (progressAdapter.itemCount == 0) {
            noGoodText.visibility = View.VISIBLE
        } else {
            noGoodText.visibility = View.GONE
        }
    }

    private fun setBadRecyclerView() {
        val linearLayoutManager = LinearLayoutManager(requireContext())
        badRecyclerView.setHasFixedSize(true)

        val badFactors: MutableList<Factor> = LinkedList()
        val badMap = mutableMapOf<Factor, Int>()
        val finalMap = mutableMapOf<Factor, Int>()
        var badCount = 0

        if (!isDemo) {
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
        } else {
            badCount = 2
            finalMap[Factor(null, "coffee", "ic_coffee")] = 1
            finalMap[Factor(null, "nose", "ic_nose")] = 1
        }

        progressAdapter = FactorProgressAdapter(
            finalMap as LinkedHashMap<Factor, Int>,
            badCount,
            false,
            requireActivity().applicationContext
        )
        badRecyclerView.layoutManager = linearLayoutManager
        badRecyclerView.adapter = progressAdapter
        if (progressAdapter.itemCount == 0) {
            noBadText.visibility = View.VISIBLE
        } else {
            noBadText.visibility = View.GONE
        }
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
            allRecordings = recordings
            if (recordings != null && recordings.isNotEmpty()) {
                isDemo = false
                demoText.visibility = View.GONE
                weekChip.isClickable = true
                monthChip.isClickable = true
                yearChip.isClickable = true
            } else {
                demoText.visibility = View.VISIBLE
                weekChip.isClickable = false
                monthChip.isClickable = false
                yearChip.isClickable = false
            }
            initCharts()
        }
        loaRecordings()
    }

    private fun loaRecordings() {
        lifecycleScope.launch {
            viewModel.recordings.observe(viewLifecycleOwner, observerRecordings)
        }
    }

    override fun onClick(v: View?) {
        val subscribed = (requireActivity() as MainActivity).subscribed
        if (subscribed) {
            when (v!!.id) {
                R.id.week_chip -> {
                    viewModel.setRecordings(7)
                    chartDays = 7
                }
                R.id.month_chip -> {
                    viewModel.setRecordings(30)
                    chartDays = 30
                }
                R.id.year_chip -> {
                    viewModel.setRecordings(365)
                    chartDays = 12
                }
            }
        } else {
            val builder = AlertDialog.Builder(requireContext(), R.style.AlertDialogCustom)
            with(builder)
            {
                setTitle(getString(R.string.subscribe))
                setMessage(getString(R.string.sub_stat))
                setOnDismissListener {
                    startActivity(Intent(requireContext(), SubscribeActivity::class.java))
                }
                show()
            }
        }
    }
}
