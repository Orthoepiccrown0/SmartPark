package com.epiccrown.smartpark.view.base

import android.content.ContentResolver
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.epiccrown.smartpark.repository.Repository
import com.epiccrown.smartpark.view.MainViewModel
import java.io.ByteArrayOutputStream


abstract class BaseFragment : Fragment() {

    val vm: MainViewModel by activityViewModels { MainViewModel.Factory(Repository()) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = getFragmentView(inflater, container)
        prepareStage()
        setListeners()
        return view
    }

    abstract fun setListeners()

    abstract fun prepareStage()

    abstract fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View

    fun Bitmap.getBase64(): String? {
        return try {
            val byteArrayOutputStream = ByteArrayOutputStream()
            this.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
            val byteArray: ByteArray = byteArrayOutputStream.toByteArray()
            Base64.encodeToString(byteArray, Base64.DEFAULT);
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            null
        }

    }

    fun uriToBitmap(selectedFileUri: Uri): Bitmap? {
        var bitmap: Bitmap? = null
        val contentResolver: ContentResolver? = activity?.contentResolver
        contentResolver?:return null
        try {
            bitmap = if (Build.VERSION.SDK_INT < 28) {
                MediaStore.Images.Media.getBitmap(contentResolver, selectedFileUri)
            } else {
                val source: ImageDecoder.Source =
                    ImageDecoder.createSource(contentResolver, selectedFileUri)
                ImageDecoder.decodeBitmap(source)
            }

        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }

        return bitmap
    }

}