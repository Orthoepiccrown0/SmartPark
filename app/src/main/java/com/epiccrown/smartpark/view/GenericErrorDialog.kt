package com.epiccrown.smartpark.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.SheetInfoCarHasDebitBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class GenericErrorDialog(

) : BottomSheetDialogFragment() {

    private lateinit var binding: SheetInfoCarHasDebitBinding

    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetInfoCarHasDebitBinding.inflate(inflater, container, false)
        binding.confirm.setOnClickListener { dismiss() }
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        return binding.root
    }



}