package com.epiccrown.smartpark.view.admin.fragments

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import com.epiccrown.smartpark.databinding.FragmentHomeAdminBinding
import com.epiccrown.smartpark.model.internal.AdminConfiguration
import com.epiccrown.smartpark.model.request.ProcessDataRequest
import com.epiccrown.smartpark.model.response.ProcessDataResponse
import com.epiccrown.smartpark.repository.AdminRepository
import com.epiccrown.smartpark.repository.network.NetworkResult
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.admin.AdminConfigurationActivity
import com.epiccrown.smartpark.view.base.BaseFragment
import id.zelory.compressor.Compressor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors


class HomeFragment : BaseFragment() {
    private lateinit var adminConfig: AdminConfiguration
    private lateinit var binding: FragmentHomeAdminBinding
    private lateinit var cameraExecutor: ExecutorService

    private var imageCapture: ImageCapture? = null

    private var photo: File? = null
    private var compressedBitmap: Bitmap? = null

    private val vm: AdminViewModel by viewModels { AdminViewModel.Factory(AdminRepository()) }

    private val configLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == AppCompatActivity.RESULT_OK)
                prepareStage()
        }


    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            if (it.all { it.value })
                prepareStage()
            else
                showPermissionsError()
        }

    private fun showPermissionsError() {
        //todo: set error
    }

    override fun CoroutineScope.start() {
        launch {
            vm.data.collect() {
                when (it) {
                    is NetworkResult.Loading -> {
                        //todo: set loader
                        showLoading()
                    }

                    is NetworkResult.Success -> {
                        setResult(it.data)
                    }

                    is NetworkResult.Error -> {
                        //todo: show error
                        unlockShutter()
                    }
                }
            }
        }
    }

    override fun setListeners() {
        binding.takePhoto.setOnClickListener {
            takePhoto()
        }

        binding.configuration.setOnClickListener {
            openConfiguration()
        }
    }

    override fun prepareStage() {
        val pf = UserPreferences(requireContext())
        if (allPermissionsGranted()) {
            startCamera()
        } else {
            permissionLauncher.launch(REQUIRED_PERMISSIONS)
        }

        cameraExecutor = Executors.newSingleThreadExecutor()
        val config = pf.getAdminConfiguration()
        if (config == null) {
            openConfiguration()
        } else {
            setConfig(config)
        }
    }

    private fun setConfig(config: AdminConfiguration) {
        adminConfig = config
        binding.zone.text = config.selectedZone.zoneName
        binding.park.text = config.selectedPark.name
    }

    private fun openConfiguration() {
        configLauncher.launch(Intent(requireContext(), AdminConfigurationActivity::class.java))
    }

    private fun takePhoto() {
        val imageCapture = imageCapture ?: return
        activity ?: return
        showLoading()
        // Create time stamped name and MediaStore entry.
        val name = SimpleDateFormat(FILENAME_FORMAT, Locale.getDefault())
            .format(System.currentTimeMillis())
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, name)
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.P) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PlateFinder")
            }
        }

        // Create output options object which contains file + metadata
        val outputOptions = ImageCapture.OutputFileOptions
            .Builder(
                requireActivity().contentResolver,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                contentValues
            )
            .build()

        // Set up image capture listener, which is triggered after photo has
        // been taken
        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(requireContext()),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }

                override fun
                        onImageSaved(output: ImageCapture.OutputFileResults) {
                    val msg = "Photo capture succeeded: ${output.savedUri}"
                    processImage(output.savedUri)
                    Log.d(TAG, msg)
                }
            }
        )
    }

    private fun processImage(savedUri: Uri?) {
        if (savedUri != null) {
            lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    val bitmap = uriToBitmap(savedUri)
                    if (bitmap != null) {
                        try {
                            val filename =
                                SimpleDateFormat(
                                    "yyyy-DD-dd-MM-mm-ss",
                                    Locale.getDefault()
                                ).format(Date())
                            val originalPhoto: File =
                                File(requireContext().filesDir, "$filename.jpg")
                            val fOut = FileOutputStream(originalPhoto)
                            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOut)
                            val compressedPhoto =
                                Compressor.compress(requireContext(), originalPhoto)
                            photo = compressedPhoto

                            compressedBitmap = BitmapFactory.decodeFile(compressedPhoto.path)
                            val request = compressedBitmap?.getBase64()?.let {
                                ProcessDataRequest(
                                    it
                                )
                            }
                            if (request != null) {
                                vm.processData(request)
                            }

//                            originalPhoto.delete()
                        } catch (e: java.lang.Exception) {
                            e.printStackTrace()
                            //todo: show error
                        }
                    } else {
                        //todo: show error
                    }
                }
            }
        }
    }

    private fun unlockShutter() {
        binding.loading.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(150)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                binding.loading.visibility = View.GONE
                binding.takePhoto.visibility = View.VISIBLE

                binding.takePhoto.scaleX = 0f
                binding.takePhoto.scaleY = 0f

                binding.takePhoto.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }.start()
    }

    private fun showLoading() {
        binding.takePhoto.animate()
            .scaleX(0f)
            .scaleY(0f)
            .setDuration(150)
            .setInterpolator(AccelerateInterpolator())
            .withEndAction {
                binding.takePhoto.visibility = View.GONE
                binding.loading.visibility = View.VISIBLE

                binding.loading.scaleX = 0f
                binding.loading.scaleY = 0f

                binding.loading.animate()
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(150)
                    .setInterpolator(OvershootInterpolator())
                    .start()
            }.start()
    }

    private fun setResult(data: ProcessDataResponse?) {
        unlockShutter()
        if (data != null && data.results.isNotEmpty()) {
            PlateResultDialog(data, compressedBitmap) {
                Toast.makeText(requireContext(), "looking for $it", Toast.LENGTH_SHORT).show()
            }.show(parentFragmentManager, null)
        } else {
            //todo: show error
        }
    }


    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())
        imageCapture = ImageCapture.Builder().build()
        cameraProviderFuture.addListener({
            // Used to bind the lifecycle of cameras to the lifecycle owner
            val cameraProvider: ProcessCameraProvider = cameraProviderFuture.get()

            // Preview
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.viewFinder.surfaceProvider)
                }

            // Select back camera as a default
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

            try {
                // Unbind use cases before rebinding
                cameraProvider.unbindAll()

                // Bind use cases to camera
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )

            } catch (exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }

        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View {
        binding = FragmentHomeAdminBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
//        photo?.delete()
    }

    private fun allPermissionsGranted() = REQUIRED_PERMISSIONS.all {
        ContextCompat.checkSelfPermission(
            requireContext(), it
        ) == PackageManager.PERMISSION_GRANTED
    }

    companion object {
        private const val TAG = "PlateFinder"
        private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"
        private val REQUIRED_PERMISSIONS =
            mutableListOf(
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO
            ).apply {
                if (Build.VERSION.SDK_INT <= Build.VERSION_CODES.P) {
                    add(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                }
            }.toTypedArray()
    }
}