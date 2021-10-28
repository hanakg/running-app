package com.example.runningapp.ui.fragments

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.runningapp.R
import com.example.runningapp.ui.viewmodels.MainViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.android.synthetic.main.fragment_run.*

@AndroidEntryPoint
class TrackingFragment:Fragment(R.layout.fragment_tracking) {
    private val viewModel: MainViewModel by viewModels()

}