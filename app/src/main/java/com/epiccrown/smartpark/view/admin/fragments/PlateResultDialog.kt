package com.epiccrown.smartpark.view.admin.fragments

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.*
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.BottomDialogResultBinding
import com.epiccrown.smartpark.model.response.ProcessDataResponse
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class PlateResultDialog(
    val data: ProcessDataResponse,
    val bitmap: Bitmap?,
    val selectedPlate: (String?) -> Unit,
) :
    BottomSheetDialogFragment() {

    private var firstPlate: String? = null
    private var secondPlate: String? = null
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
        binding.firstPlate.setOnClickListener {
            val clipboard: ClipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("PlateFinder", firstPlate ?: "Not found")
            clipboard.setPrimaryClip(clip)
            selectedPlate(firstPlate)
            dismiss()
        }

        binding.secondPlate.setOnClickListener {
            val clipboard: ClipboardManager =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip: ClipData = ClipData.newPlainText("PlateFinder", secondPlate ?: "Not found")
            clipboard.setPrimaryClip(clip)
            selectedPlate(secondPlate)
            dismiss()
        }


        binding.retry.setOnClickListener {
            dismiss()
        }
    }

    private fun setData() {
        firstPlate = findBest()
        if (firstPlate != null) {
            binding.resultPlateTxt.text = firstPlate
            if (data.results.first().candidates.isEmpty()) {
                binding.secondPlate.visibility = View.GONE
            } else {
                secondPlate = data.results.first().candidates.first().plate
                if (secondPlate!!.matches(plateNumberRegex) && secondPlate != firstPlate)
                    binding.resultPlate2Txt.text = secondPlate
                else
                    binding.secondPlate.visibility = View.GONE
            }

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