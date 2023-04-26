package com.epiccrown.smartpark.view.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.SheetNewParkBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewParkDialog : BottomSheetDialogFragment() {

    private lateinit var binding: SheetNewParkBinding

    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SheetNewParkBinding.inflate(inflater, container, false)

        return binding.root
    }


}