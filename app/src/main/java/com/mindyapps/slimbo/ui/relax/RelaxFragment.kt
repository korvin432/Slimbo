package com.mindyapps.slimbo.ui.relax

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import com.mindyapps.slimbo.R

class RelaxFragment : Fragment() {

    private lateinit var relaxViewModel: RelaxViewModel

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {
        relaxViewModel =
                ViewModelProviders.of(this).get(RelaxViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_relax, container, false)

        return root
    }
}
