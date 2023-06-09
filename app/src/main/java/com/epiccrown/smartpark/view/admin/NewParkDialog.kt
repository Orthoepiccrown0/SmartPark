package com.epiccrown.smartpark.view.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.SheetNewParkBinding
import com.epiccrown.smartpark.model.request.AddParkRequest
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import java.math.BigDecimal

class NewParkDialog(
    private val zoneId: Int,
    val result: (AddParkRequest) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: SheetNewParkBinding
    private var name: String? = null
    private var slots: Int? = null
    private var minTime: Int = 5
    private var price: BigDecimal? = null
    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = SheetNewParkBinding.inflate(inflater, container, false)
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.confirm.setOnClickListener {
            result(AddParkRequest(zoneId, name!!, price!!, minTime, slots!!))
            dismiss()
        }

        binding.parkname.addTextChangedListener {
            name = if (it.isNullOrBlank()) null else it.toString()
            checkData()
        }
        binding.lots.addTextChangedListener {
            slots = if (it.isNullOrBlank()) null else it.toString().toInt()
            checkData()
        }
        binding.price.addTextChangedListener {
            price = if (it.isNullOrBlank()) null else BigDecimal(it.toString())
            checkData()
        }
    }

    private fun checkData() {
        var isValid = true

        if (name == null)
            isValid = false

        if (slots == null)
            isValid = false

        if (price == null)
            isValid = false

        binding.confirm.isEnabled = isValid
    }


}