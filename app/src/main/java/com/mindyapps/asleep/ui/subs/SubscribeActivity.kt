package com.mindyapps.asleep.ui.subs

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import com.mindyapps.asleep.R
import kotlinx.android.synthetic.main.activity_subscribe.*

class SubscribeActivity : AppCompatActivity(), View.OnClickListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subscribe)
        theme.applyStyle(R.style.AppThemeTransparent, true)
        supportActionBar!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        supportActionBar!!.title = ""

        month_layout.setOnClickListener(this)
        three_month_layout.setOnClickListener(this)
        year_layout.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when (v!!.id){
            R.id.month_layout -> {
                month_layout.setBackgroundResource(R.drawable.sub_button_gradient)
                three_month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                year_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
            }
            R.id.three_month_layout -> {
                three_month_layout.setBackgroundResource(R.drawable.sub_button_gradient)
                month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                year_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
            }
            R.id.year_layout -> {
                year_layout.setBackgroundResource(R.drawable.sub_button_gradient)
                three_month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
                month_layout.setBackgroundColor(ContextCompat.getColor(this, R.color.card_bg))
            }
        }
    }

}
