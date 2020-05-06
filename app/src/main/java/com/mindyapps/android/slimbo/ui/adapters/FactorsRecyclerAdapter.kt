package com.mindyapps.android.slimbo.ui.adapters

import android.content.Context
import android.content.res.Resources
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.mindyapps.android.slimbo.R
import com.mindyapps.android.slimbo.data.model.Factor
import kotlinx.android.synthetic.main.factor_item.view.*


class FactorsRecyclerAdapter(
    private var factors: MutableList<Factor>,
    private var selectedFactors: MutableList<Factor>,
    private var context: Context
) : RecyclerView.Adapter<FactorsRecyclerAdapter.FactorsHolder>() {

    var onItemClick: ((Factor, Boolean) -> Unit)? = null

    override fun onCreateViewHolder(
        viewGroup: ViewGroup,
        viewType: Int
    ): FactorsHolder {
        val itemView: View =
            LayoutInflater.from(viewGroup.context).inflate(R.layout.factor_item, viewGroup, false)
        return FactorsHolder(itemView)
    }

    override fun getItemCount(): Int {
        return factors.size
    }

    override fun onBindViewHolder(holder: FactorsHolder, position: Int) {
        val factor: Factor = factors[position]
        setPropertiesForFactorsViewHolder(holder, factor)
    }

    fun setFactors(newFactors: List<Factor>) {
        factors.clear()
        factors.addAll(newFactors)
        notifyDataSetChanged()
    }

    private fun setPropertiesForFactorsViewHolder(
        factorsViewHolder: FactorsHolder,
        factor: Factor
    ) {
        val stringId: Int = context.resources.getIdentifier(
            factor.name, "string",
            context.packageName
        )

        factorsViewHolder.name.text = context.getString(stringId)
        factorsViewHolder.image.tag = factor.resource_name
        val resourceId: Int = context.resources.getIdentifier(
            factor.resource_name, "drawable",
            context.packageName
        )
        factorsViewHolder.image.setImageDrawable(ContextCompat.getDrawable(context, resourceId))
        if (selectedFactors.size != 0)  {
            if (selectedFactors.contains(factor)) {
                val resourceId: Int = context.resources.getIdentifier(
                    factor.resource_name + "_filled", "drawable",
                    context.packageName
                )
                factorsViewHolder.image.setImageDrawable(
                    ContextCompat.getDrawable(
                        context,
                        resourceId
                    )
                )
                factorsViewHolder.image.setColorFilter(
                    ContextCompat.getColor(
                        context,
                        R.color.colorFilled
                    )
                )
                factorsViewHolder.image.tag = factor.resource_name + "_filled"
            }
        }
    }

    inner class FactorsHolder(private val view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView by lazy { view.factor_name }
        val image: ImageView by lazy { view.factor_image }

        init {
            itemView.setOnClickListener {
                var name = factors[adapterPosition].resource_name
                var selected = false
                val tint: Int
                if (!image.tag.toString().contains("filled")) {
                    name = factors[adapterPosition].resource_name + "_filled"
                    tint = ContextCompat.getColor(context, R.color.colorFilled)
                    selected = true
                } else {
                    name!!.replace("_filled", "")
                    tint = ContextCompat.getColor(context, R.color.factor_tint)
                }

                val resourceId: Int = context.resources.getIdentifier(
                    name, "drawable",
                    context.packageName
                )
                image.setImageDrawable(ContextCompat.getDrawable(context, resourceId))
                image.setColorFilter(tint)
                image.tag = name
                onItemClick?.invoke(factors[adapterPosition], selected)
            }
        }
    }
}