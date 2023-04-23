package com.epiccrown.smartpark.view.base

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.CoroutineScope


abstract class BaseActivity : AppCompatActivity(){

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

    class Observer :DefaultLifecycleObserver{
        override fun onCreate(owner: LifecycleOwner) {
            super.onCreate(owner)
            Log.e(BaseActivity::class.simpleName,"onCreate")
        }

        override fun onDestroy(owner: LifecycleOwner) {
            super.onDestroy(owner)
            Log.e(BaseActivity::class.simpleName,"onDestroy")
        }

        override fun onPause(owner: LifecycleOwner) {
            super.onPause(owner)
            Log.e(BaseActivity::class.simpleName,"onPause")
        }

        override fun onResume(owner: LifecycleOwner) {
            super.onResume(owner)
            Log.e(BaseActivity::class.simpleName,"onResume")
        }

        override fun onStart(owner: LifecycleOwner) {
            super.onStart(owner)
            Log.e(BaseActivity::class.simpleName,"onStart")
        }

        override fun onStop(owner: LifecycleOwner) {
            super.onStop(owner)
            Log.e(BaseActivity::class.simpleName,"onStop")
        }
    }

}