package com.epiccrown.smartpark.view.common

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.SheetNewParkBinding
import com.epiccrown.smartpark.databinding.SheetSinglePickerBinding
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class SingleSelectionPicker<T>(
    private val data: Array<T>,
    private val title: Int?,
    private val subtitle: Int?,
    var selectedItem: T? = null,
    val selection: (T) -> Unit
) : BottomSheetDialogFragment() {

    private lateinit var binding: SheetSinglePickerBinding

    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = SheetSinglePickerBinding.inflate(inflater, container, false)
        setData()
        setListeners()
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            state = BottomSheetBehavior.STATE_EXPANDED
        }
        return binding.root
    }

    private fun setListeners() {
        binding.cancel.setOnClickListener {
            dismiss()
        }
        binding.confirm.setOnClickListener {
            if (selectedItem != null) {
                selection(selectedItem!!)
                dismiss()
            }
        }
    }

    private fun setData() {
        if (title == null) binding.title.visibility = View.GONE
        else binding.title.text = getString(title)

        if (subtitle == null) binding.subtitle.visibility = View.GONE
        else binding.subtitle.text = getString(subtitle)

        val adapter = ArrayAdapter(requireContext(), R.layout.item_selection_list, data)
        binding.list.adapter = adapter
        binding.list.onItemClickListener =
            AdapterView.OnItemClickListener { p0, p1, position, p3 ->
                selectedItem = data[position]
                binding.confirm.isEnabled = true
            }
    }


}