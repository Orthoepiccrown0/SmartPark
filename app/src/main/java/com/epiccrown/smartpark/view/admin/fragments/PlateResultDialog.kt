package com.epiccrown.smartpark.view.admin.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.BottomDialogResultBinding
import com.epiccrown.smartpark.model.response.ProcessDataResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlateResultDialog(val data: ProcessDataResponse, val bitmap: Bitmap?) :
    BottomSheetDialogFragment() {

    private var plateNumber: String? = null
    private lateinit var binding: BottomDialogResultBinding
    private val plateNumberRegex = "[A-Za-z]{2}\\d{3}[A-Za-z]{2}".toRegex()
    override fun getTheme(): Int {
        return R.style.BaseBottomSheet
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = BottomDialogResultBinding.inflate(inflater, container, false)
        setData()
        setListeners()
        return binding.root
    }

    private fun setListeners() {
        binding.plate.setOnClickListener {
            val clipboard: ClipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("PlateFinder", plateNumber ?: "Not found")
            clipboard.setPrimaryClip(clip)
            Toast.makeText(requireContext(), "La targa copiata negli apppunti", Toast.LENGTH_SHORT)
                .show()
        }

        binding.retry.setOnClickListener {
            dismiss()
        }
    }

    private fun setData() {
        plateNumber = findBest()
        if (plateNumber != null) {
            binding.resultPlateTxt.text = plateNumber
            val candidatesBuilder = StringBuilder()
            data.results.first().candidates.take(5).forEachIndexed { _, result ->
                if (result.plate!=plateNumber && result.plate.matches(plateNumberRegex)) {
                    candidatesBuilder.append("${result.plate}\n")
                }
            }
            if (candidatesBuilder.isNotEmpty())
                binding.alternativesText.text = candidatesBuilder.toString()
            else
                binding.alternatives.visibility = View.GONE

            drawPlateRegion(data)
        } else {
            //todo: show error
        }
    }

    private fun findBest(): String? {
        return if (data.results.first().plate.matches(plateNumberRegex)) {
            data.results.first().plate
        } else {
            var bestCandidate: String? = null
            data.results.first().candidates.forEach {
                if (it.plate.matches(plateNumberRegex) && bestCandidate == null)
                    bestCandidate = it.plate
            }
            return bestCandidate
        }
    }

    private fun drawPlateRegion(data: ProcessDataResponse) {
        if (bitmap != null && data.results.isNotEmpty()) {
            val tempBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true);

            val canvas = Canvas(tempBitmap)

            val outlinePaint = Paint().apply {
                style = Paint.Style.STROKE
                strokeWidth = 10f
                color = Color.RED
            }

            // Set up the paint for the fill
            val fillPaint = Paint().apply {
                style = Paint.Style.FILL
                color = Color.parseColor("#80000000") // Use 50% opacity black color
            }


            val path = Path()
            data.results.first().coordinates.forEachIndexed { index, coordinate ->
                if (index == 0) {
                    path.moveTo(coordinate.x, coordinate.y)
                } else {
                    path.lineTo(coordinate.x, coordinate.y)
                }
            }
            canvas.drawPath(path, outlinePaint)
            canvas.drawPath(path, fillPaint)
            binding.image.setImageBitmap(tempBitmap)
            binding.image.draw(canvas)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.apply {
            this.state = BottomSheetBehavior.STATE_EXPANDED
        }
    }


}