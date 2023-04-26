package com.epiccrown.smartpark.view.admin

import android.view.View
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityMainAdminBinding
import com.epiccrown.smartpark.view.base.BaseActivity
import kotlinx.coroutines.CoroutineScope

class AdminActivity : BaseActivity() {
    private lateinit var binding: ActivityMainAdminBinding

    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        setupNavigation()
    }

    override fun setListeners() {

    }

    private fun setupNavigation() {
        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
    }

    override fun getActivityView(): View {
        binding = ActivityMainAdminBinding.inflate(layoutInflater)
        return binding.root
    }
}