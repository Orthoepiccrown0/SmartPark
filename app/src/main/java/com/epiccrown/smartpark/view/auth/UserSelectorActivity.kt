package com.epiccrown.smartpark.view.auth

import android.content.Intent
import android.view.View
import com.epiccrown.smartpark.R
import com.epiccrown.smartpark.databinding.ActivityUserSelectorBinding
import com.epiccrown.smartpark.utils.preferences.UserPreferences
import com.epiccrown.smartpark.view.MainActivity
import com.epiccrown.smartpark.view.base.BaseActivity
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayout.Tab
import kotlinx.coroutines.CoroutineScope

class UserSelectorActivity : BaseActivity() {
    private lateinit var binding: ActivityUserSelectorBinding
    private var selectedUser = UserType.ADMIN
    private val pf = UserPreferences(this)
    override fun CoroutineScope.observeStuff() {

    }

    override fun prepareStage() {
        binding.admin.isSelected = true
        binding.user.setOnClickListener {
            it.isSelected = true
            binding.admin.isSelected = false
            selectedUser = UserType.USER
            binding.userLayout.visibility = View.VISIBLE
            binding.adminLayout.visibility = View.GONE
        }

        binding.admin.setOnClickListener {
            it.isSelected = true
            binding.user.isSelected = false
            selectedUser = UserType.ADMIN
            binding.userLayout.visibility = View.GONE
            binding.adminLayout.visibility = View.VISIBLE
        }
    }

    override fun setListeners() {
        binding.confirm.setOnClickListener {
            pf.setAdmin(selectedUser == UserType.ADMIN)
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }
    }

    override fun getActivityView(): View {
        binding = ActivityUserSelectorBinding.inflate(layoutInflater)
        return binding.root
    }

    enum class UserType {
        ADMIN,
        USER
    }
}