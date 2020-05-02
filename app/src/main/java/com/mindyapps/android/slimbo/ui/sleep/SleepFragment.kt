package com.mindyapps.android.slimbo.ui.sleep

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProviders
import com.mindyapps.android.slimbo.R


class SleepFragment : Fragment() {

    private lateinit var sleepViewModel: SleepViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        sleepViewModel =
            ViewModelProviders.of(this).get(SleepViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_sleep, container, false)

        val loginActivityBackground: Drawable =
            root.findViewById<RelativeLayout>(R.id.sleep_layout).background
        loginActivityBackground.alpha = 35
        return root
    }
}
