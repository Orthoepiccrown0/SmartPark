package com.epiccrown.smartpark.view

import android.content.Intent
import android.view.View
import androidx.activity.viewModels
import com.epiccrown.smartpark.databinding.ActivityMainBinding
import com.epiccrown.smartpark.repository.UserRepository
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.admin.AdminActivity
import com.epiccrown.smartpark.view.auth.UserLoginActivity
import com.epiccrown.smartpark.view.auth.UserSelectorActivity
import com.epiccrown.smartpark.view.base.BaseActivity
import com.epiccrown.smartpark.view.user.UserActivity
import kotlinx.coroutines.CoroutineScope

class MainActivity : BaseActivity() {
    private lateinit var binding: ActivityMainBinding
    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        //check user type
        val pf = UserPreferences(this)
        if (pf.isUserTypeSelected()) {
            if (pf.isAdmin()) {
                startActivity(Intent(this, AdminActivity::class.java))
                finish()
            } else {
                //show user UI
                if(pf.getUser()!=null){
                    startActivity(Intent(this, UserActivity::class.java))
                    finish()
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