package com.mindyapps.slimbo.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.slimbo.R
import com.mindyapps.slimbo.data.model.Factor
import kotlinx.android.synthetic.main.factor_progress_item.view.*


class FactorProgressAdapter(
    private var factors: LinkedHashMap<Factor, Int>,
    private var count: Int,
    val isGood: Boolean,
    private var context: Context
) : RecyclerView.Adapter<FactorProgressAdapter.FactorsHolder>() {

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): FactorsHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context)
                .inflate(R.layout.factor_progress_item, viewGroup, false)
        return FactorsHolder(itemView)
    }

    override fun getItemCount(): Int {
        return factors.size
    }

    override fun onBindViewHolder(holder: FactorsHolder, position: Int) {
        setPropertiesForFactorsViewHolder(
            holder, ArrayList<Factor>(factors.keys)[position],
            ArrayList<Int>(factors.values)[position]
        )
    }

    private fun setPropertiesForFactorsViewHolder(
        factorsViewHolder: FactorsHolder,
        factor: Factor,
        factorCount: Int
    ) {
        val stringId: Int = context.resources.getIdentifier(
            factor.name, "string",
            context.packageName
        )
        factorsViewHolder.name.text = context.getString(stringId)
        factorsViewHolder.percentage.text = (100 * factorCount / count).toString() + " %"
        factorsViewHolder.progressBar.progress = factorCount
    }

    inner class FactorsHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView by lazy { view.factor_name }
        val percentage: TextView by lazy { view.percentage }
        val progressBar: ProgressBar by lazy { view.sleep_progress }

        init {
            progressBar.max = count
            if (!isGood) {
                progressBar.progressDrawable =
                    ContextCompat.getDrawable(context, R.drawable.bad_progress)
            }
        }
    }
}