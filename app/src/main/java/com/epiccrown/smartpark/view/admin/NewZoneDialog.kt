package com.epiccrown.smartpark.view.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.SheetNewParkBinding
import com.epiccrown.smartpark.databinding.SheetNewZoneBinding
import com.epiccrown.smartpark.model.request.AddZoneRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class NewZoneDialog(val result: (AddZoneRequest) -> Unit) : BottomSheetDialogFragment() {

    private lateinit var binding: SheetNewZoneBinding
    private var name: String? = null
    private var description: String? = null

    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetNewZoneBinding.inflate(inflater, container, false)
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.confirm.setOnClickListener {
            result(AddZoneRequest(name!!, description!!))
            dismiss()
        }

        binding.zone.addTextChangedListener {
            name = if (it.isNullOrBlank()) null else it.toString()
            checkData()
        }
        binding.description.addTextChangedListener {
            description = if (it.isNullOrBlank()) null else it.toString()
            checkData()
        }
    }

    private fun checkData() {
        var isValid = true

        if (name == null)
            isValid = false

        if (description == null)
            isValid = false

        binding.confirm.isEnabled = isValid
    }

}