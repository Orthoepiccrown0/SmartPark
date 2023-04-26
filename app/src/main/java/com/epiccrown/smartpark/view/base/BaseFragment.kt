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
import androidx.lifecycle.lifecycleScope
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.view.MainViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.io.ByteArrayOutputStream
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone


abstract class BaseFragment : Fragment() {

    private val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = getFragmentView(inflater, container)
        serverDateFormat.timeZone = TimeZone.getTimeZone("GTM")
        prepareStage()
        setListeners()
        viewLifecycleOwner.lifecycleScope.start()
        return view
    }

    abstract fun CoroutineScope.start()

    abstract fun setListeners()

    abstract fun prepareStage()

    abstract fun getFragmentView(inflater: LayoutInflater, container: ViewGroup?): View

    fun String.fromServerDate(desiredFormat: String): String {
        val formatter = SimpleDateFormat(desiredFormat, Locale.getDefault())
        val date = serverDateFormat.parse(this)
        return try {
            formatter.format(date)
        } catch (e: Exception) {
            e.printStackTrace()
            "--"
        }
    }

    fun String.fromServerDate(): Date {
        return try {
            serverDateFormat.parse(this)!!
        } catch (e: Exception) {
            e.printStackTrace()
            Date()
        }
    }

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
        contentResolver ?: return null
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