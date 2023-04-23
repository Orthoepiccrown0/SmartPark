package com.epiccrown.smartpark.view.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.databinding.FragmentHomeUserBinding
import com.epiccrown.smartpark.view.base.BaseFragment

class HomeFragment : BaseFragment() {
    private lateinit var binding: FragmentHomeUserBinding

    override fun setListeners() {
    }

    override fun prepareStage() {
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentHomeUserBinding.inflate(inflater, container, false)
        return binding.root
    }
}