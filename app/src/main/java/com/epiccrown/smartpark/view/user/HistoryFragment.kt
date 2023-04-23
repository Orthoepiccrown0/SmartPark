package com.epiccrown.smartpark.view.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.databinding.FragmentHistoryUserBinding
import com.epiccrown.smartpark.view.base.BaseFragment

class HistoryFragment:BaseFragment() {
    private lateinit var binding: FragmentHistoryUserBinding

    override fun setListeners() {
    }

    override fun prepareStage() {
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentHistoryUserBinding.inflate(inflater,container,false)
        return binding.root
    }
}