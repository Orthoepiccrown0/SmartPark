package com.epiccrown.smartpark.view.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


abstract class BaseActivity : AppCompatActivity() {
    private val serverDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(getActivityView())
        prepareStage()
        setListeners()
        lifecycle.addObserver(Observer())
        lifecycleScope.observeStuff()
    }

    abstract fun CoroutineScope.observeStuff()

    abstract fun prepareStage()

    abstract fun setListeners()

    abstract fun getActivityView(): View

    fun launch(launchItem: suspend () -> Unit) {
        lifecycleScope.launch { launchItem() }
    }

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

    class Observer : DefaultLifecycleObserver {
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            Log.e(BaseActivity::class.simpleName, "onCreate")
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            Log.e(BaseActivity::class.simpleName, "onDestroy")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Log.e(BaseActivity::class.simpleName, "onPause")
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            Log.e(BaseActivity::class.simpleName, "onResume")
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            Log.e(BaseActivity::class.simpleName, "onStart")
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            Log.e(BaseActivity::class.simpleName, "onStop")
        }
    }

}