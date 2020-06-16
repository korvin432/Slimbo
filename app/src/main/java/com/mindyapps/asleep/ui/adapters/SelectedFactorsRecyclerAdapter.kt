package com.mindyapps.asleep.ui.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.asleep.R
import com.mindyapps.asleep.data.model.Factor
import kotlinx.android.synthetic.main.selected_factor_item.view.*


class SelectedFactorsRecyclerAdapter(
    private var factors: MutableList<Factor>,
    private var context: Context
) : RecyclerView.Adapter<SelectedFactorsRecyclerAdapter.FactorsHolder>() {


    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): FactorsHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.selected_factor_item, viewGroup, false)
        return FactorsHolder(itemView)
    }

    override fun getItemCount(): Int {
        return factors.size
    }

    override fun onBindViewHolder(holder: FactorsHolder, position: Int) {
        val factor: Factor = factors[position]
        setPropertiesForFactorsViewHolder(holder, factor)
    }


    private fun setPropertiesForFactorsViewHolder(
        factorsViewHolder: FactorsHolder,
        factor: Factor
    ) {
        val resourceId: Int = context.resources.getIdentifier(
            factor.resource_name + "_filled", "drawable",
            context.packageName
        )
        factorsViewHolder.image.setImageDrawable(ContextCompat.getDrawable(context,resourceId))
    }

    inner class FactorsHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val image: ImageView by lazy { view.selected_image }
    }
}
