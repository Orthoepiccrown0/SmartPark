package com.epiccrown.smartpark.view

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.epiccrown.smartpark.databinding.ActivityMainBinding
import com.epiccrown.smartpark.repository.Repository
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.auth.UserLoginActivity
import com.epiccrown.smartpark.view.auth.UserSelectorActivity
import com.epiccrown.smartpark.view.base.BaseActivity
import kotlinx.coroutines.CoroutineScope

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    private val vm: MainViewModel by viewModels { MainViewModel.Factory(Repository()) }
    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        //check user type
        val pf = UserPreferences(this)
        if (pf.isUserTypeSelected()) {
            if (pf.isAdmin()) {

            } else {
                //show user UI
                if(pf.getUser()!=null){

                }else{
                    startActivity(Intent(this, UserLoginActivity::class.java))
                    finish()
                }
            }
        } else {
            startActivity(Intent(this, UserSelectorActivity::class.java))
            finish()
        }

    }

    override fun setListeners() {

    }

    override fun getActivityView(): View {
        binding = ActivityMainBinding.inflate(layoutInflater)
        return binding.root
    }

    private fun setupNavigation() {

    }
}