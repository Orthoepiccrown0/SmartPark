package com.epiccrown.smartpark.view.common

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.text.InputFilter
import android.view.LayoutInflater
import com.epiccrown.smartpark.databinding.DialogTextInputBinding

object TextInputDialog {
    fun showDialog(
        context: Context,
        title: Int,
        subtitle: Int,
        maxLenght: Int = -1,
        hint: Int,
        result: (String) -> Unit
    ) {
        val binding = DialogTextInputBinding.inflate(LayoutInflater.from(context))
        binding.input.hint = context.getString(hint)
        binding.title.text = context.getString(title)
        binding.subtitle.text = context.getString(subtitle)
        if (maxLenght != -1)
            binding.input.filters = arrayOf(InputFilter.LengthFilter(maxLenght))

        val dialog = AlertDialog.Builder(context)
            .setView(binding.root)
            .create()

        binding.confirm.setOnClickListener {
            result(binding.input.text.toString())
            dialog.dismiss()
        }

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        dialog.show()
    }
}