package com.mindyapps.android.slimbo.ui.music_select

import androidx.lifecycle.ViewModelProviders
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import com.mindyapps.android.slimbo.R

class SelectMusicFragment : Fragment() {

    companion object {
        fun newInstance() = SelectMusicFragment()
    }

    private lateinit var viewModel: SelectMusicViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.select_music_fragment, container, false)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProviders.of(this).get(SelectMusicViewModel::class.java)
        // TODO: Use the ViewModel
    }

}
