package com.epiccrown.smartpark.view.user

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.databinding.FragmentProfileUserBinding
import com.epiccrown.smartpark.view.base.BaseFragment

class ProfileFragment : BaseFragment() {
    private lateinit var binding: FragmentProfileUserBinding

    override fun setListeners() {
    }

    override fun prepareStage() {
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentProfileUserBinding.inflate(inflater, container, false)
        return binding.root
    }
}