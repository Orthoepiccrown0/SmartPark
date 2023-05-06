package com.epiccrown.smartpark.view.admin.dialogs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.SheetInfoSuccessBinding
import com.epiccrown.smartpark.databinding.SheetInfoUserNotRegistredBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class InfoNoAccountDialog(

) : BottomSheetDialogFragment() {

    private lateinit var binding: SheetInfoUserNotRegistredBinding

    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetInfoUserNotRegistredBinding.inflate(inflater, container, false)
        binding.confirm.setOnClickListener { dismiss() }
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        return binding.root
    }



}