package com.epiccrown.smartpark.view.user

import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityMainUserBinding
import com.epiccrown.smartpark.view.base.BaseActivity
import kotlinx.coroutines.CoroutineScope

class UserActivity : BaseActivity() {
    private lateinit var binding: ActivityMainUserBinding

    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        setupNavigation()
    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        binding.navigation.setupWithNavController(navController)
    }

    override fun setListeners() {
    }

    override fun getActivityView(): View {
        binding = ActivityMainUserBinding.inflate(layoutInflater)
        return binding.root
    }
}